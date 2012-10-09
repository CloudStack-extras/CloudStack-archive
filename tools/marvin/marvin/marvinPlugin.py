import marvin
import sys
import logging
import nose.core
from marvin.cloudstackTestCase import cloudstackTestCase
from marvin import deployDataCenter
from nose.plugins.base import Plugin
from functools import partial

def testCaseLogger(message, logger=None):
    if logger is not None:
        logger.debug(message)
       
class MarvinPlugin(Plugin):
    """
    Custom plugin for the cloudstackTestCases to be run using nose
    """
    
    name = "marvin"
    def configure(self, options, config):
        self.enabled = 1
        self.enableOpt = "--with-marvin"
        self.logformat = logging.Formatter("%(asctime)s - %(levelname)s - %(name)s - %(message)s")

        if options.debug_log:
            self.logger = logging.getLogger("NoseTestExecuteEngine")
            self.debug_stream = logging.FileHandler(options.debug_log) 
            self.debug_stream.setFormatter(self.logformat)
            self.logger.addHandler(self.debug_stream)
            self.logger.setLevel(logging.DEBUG)
            
        if options.result_log:
            ch = logging.StreamHandler()
            ch.setLevel(logging.ERROR)
            ch.setFormatter(self.logformat)
            self.logger.addHandler(ch)
            self.result_stream = open(options.result_log, "w")
        else:
            self.result_stream = sys.stdout
    
        deploy = deployDataCenter.deployDataCenters(options.config) 
        deploy.loadCfg() if options.load else deploy.deploy()
        self.setClient(deploy.testClient)
        
        cfg = nose.config.Config()
        cfg.logStream = self.result_stream
        cfg.debugLog = self.debug_stream
        
        self.testrunner = nose.core.TextTestRunner(stream=self.result_stream, descriptions=True, verbosity=2, config=config)
    
    def options(self, parser, env):
        """
        Register command line options
        """
        parser.add_option("--marvin-config", action="store",
                          default=env.get('MARVIN_CONFIG', './datacenter.cfg'),
                          dest="config",
                          help="Marvin's configuration file where the datacenter information is specified [MARVIN_CONFIG]")
        parser.add_option("--result-log", action="store",
                          default=env.get('RESULT_LOG', None),
                          dest="result_log",
                          help="The path to the results file where test summary will be written to [RESULT_LOG]")
        parser.add_option("--client-log", action="store",
                          default=env.get('DEBUG_LOG', 'debug.log'),
                          dest="debug_log",
                          help="The path to the testcase debug logs [DEBUG_LOG]")
        parser.add_option("--load", action="store_true", default=False, dest="load",
                          help="Only load the deployment configuration given")
        
        Plugin.options(self, parser, env)
 
    def __init__(self):
        Plugin.__init__(self)
        
    def prepareTestRunner(self, runner):
        return self.testrunner
    
    def wantClass(self, cls):
        if issubclass(cls, cloudstackTestCase):
            return True
        return None
    
    def loadTestsFromTestCase(self, cls):
        self._injectClients(cls)
        
    def setClient(self, client):
        if client:
            self.testclient = client

    def _injectClients(self, test):
        testcaselogger = logging.getLogger("testclient.testcase.%s" % test.__name__)
        self.debug_stream.setFormatter(logging.Formatter("%(asctime)s - %(levelname)s - %(name)s - %(message)s"))
        testcaselogger.addHandler(self.debug_stream)
        testcaselogger.setLevel(logging.DEBUG)
        
        setattr(test, "testClient", self.testclient)
        setattr(test, "debug", partial(testCaseLogger, logger=testcaselogger))
        setattr(test, "clstestclient", self.testclient)
        if hasattr(test, "UserName"):
            self.testclient.createNewApiClient(test.UserName, test.DomainName, test.AcctType)
            
