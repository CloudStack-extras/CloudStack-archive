/**
 *  Copyright (C) 2011 Cloud.com.  All rights reserved.
 *
 * This software is licensed under the GNU General Public License v3 or later. 
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later
version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.cloud.agent.vmdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.network.Networks.TrafficType;
import com.cloud.storage.JavaStorageLayer;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.net.NetUtils;

/**
 * Serves vm data using embedded Jetty server
 *
 */
@Local (value={VmDataServer.class})
public class JettyVmDataServer implements VmDataServer {
    private static final Logger s_logger = Logger.getLogger(JettyVmDataServer.class);
    
    public static final String USER_DATA = "user-data";
    public static final String META_DATA = "meta-data";
    protected String _vmDataDir;
    protected Server _jetty;
    protected Map<String, String> _ipVmMap = new HashMap<String, String>();
    protected StorageLayer _fs = new JavaStorageLayer();
    
    public class VmDataServlet extends HttpServlet {

        private static final long serialVersionUID = -1640031398971742349L;
        
        JettyVmDataServer _vmDataServer;
        String _dataType; //userdata or meta-data
       
        
        public VmDataServlet(JettyVmDataServer dataServer, String dataType) {
            this._vmDataServer = dataServer;
            this._dataType = dataType;
        }
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            int port = req.getServerPort();
            if (port != 8000) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not understood");
                return;
            }
            if (_dataType.equalsIgnoreCase(USER_DATA)) {
                handleUserData(req, resp);
            } else if (_dataType.equalsIgnoreCase(META_DATA)) {
                handleMetaData(req, resp);
            }
        }
        
        protected void handleUserData(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            String requester = req.getRemoteAddr();
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK);
            String userData = _vmDataServer.getVmDataItem(requester, USER_DATA);
            if (userData != null){
                resp.getWriter().println();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found");
            }
            
        }
        
        protected void handleMetaData(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            String metadataItem = req.getPathInfo();
            String requester = req.getRemoteAddr();
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK);
            String metaData = _vmDataServer.getVmDataItem(requester, metadataItem);
            if (metaData != null) {
                resp.getWriter().println(_vmDataServer.getVmDataItem(requester, metadataItem));
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Request not found");
            }
        }
        
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        boolean success = true;
        try {
            int vmDataPort = 80;
            int fileservingPort = 8000;
            _vmDataDir = (String)params.get("vm.data.dir");
            String port = (String) params.get("vm.data.port");
            if (port != null) {
                vmDataPort = Integer.parseInt(port);
            }
            port = (String) params.get("file.server.port");
            if (port != null) {
                fileservingPort = Integer.parseInt(port);
            }
            
            if (_vmDataDir == null) {
                _vmDataDir = "/var/www/html";
            }
            success = _fs.mkdirs(_vmDataDir);
            success = success && buildIpVmMap();
            if (success) {
                setupJetty(vmDataPort, fileservingPort);
            }
        } catch (Exception e) {
            s_logger.warn("Failed to configure jetty", e);
            throw new ConfigurationException("Failed to configure jetty!!");
        }
        return success;
    }
    
    protected boolean buildIpVmMap() {
        String[] dirs = _fs.listFiles(_vmDataDir);
        for (String dir: dirs) {
            String [] path = dir.split("/");
            String vm = path[path.length -1];
            if (vm.startsWith("i-")) {
                String [] dataFiles = _fs.listFiles(dir);
                for (String dfile: dataFiles) {
                    String path2[] = dfile.split("/");
                    String ipv4file = path2[path2.length -1];
                    if (ipv4file.equalsIgnoreCase("local-ipv4")){
                        try {
                            BufferedReader input =  new BufferedReader(new FileReader(dfile));
                            String line = null; 
                            while (( line = input.readLine()) != null){
                                if (NetUtils.isValidIp(line)) {
                                    _ipVmMap.put(line, dir);
                                    s_logger.info("Found ip " + line + " for vm " + vm);
                                } else {
                                    s_logger.info("Invalid ip " + line + " for vm " + vm);
                                }
                            }
                        } catch (FileNotFoundException e) {
                            s_logger.warn("Failed to find file " + dfile);
                        } catch (IOException e) {
                           s_logger.warn("Failed to get ip address of " + vm);
                        }
                        
                    }
                }
            }
        }
        return true;
    }

    public String getVmDataItem(String requester, String dataItem) {
        String vmName = _ipVmMap.get(requester);
        if (vmName == null){
            return null;
        }
        String vmDataFile = _vmDataDir + File.separator + vmName + dataItem;
        try {
            BufferedReader input =  new BufferedReader(new FileReader(vmDataFile));
            StringBuilder result = new StringBuilder();
            String line = null; 
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
            input.close();
            return result.toString();
        } catch (FileNotFoundException e) {
            s_logger.warn("Failed to find requested file " + vmDataFile);
            return null;
        } catch (IOException e) {
            s_logger.warn("Failed to read requested file " + vmDataFile);
            return null;
        } 
    }

    private void setupJetty(int vmDataPort, int fileservingPort) throws Exception {
        _jetty  = new Server();
 
        SelectChannelConnector connector0 = new SelectChannelConnector();
        connector0.setHost("127.0.0.1");
        connector0.setPort(fileservingPort);
        connector0.setMaxIdleTime(30000);
        connector0.setRequestBufferSize(8192);
 
        SelectChannelConnector connector1 = new SelectChannelConnector();
        connector1.setHost("127.0.0.1");
        connector1.setPort(vmDataPort);
        connector1.setThreadPool(new QueuedThreadPool(5));
        connector1.setMaxIdleTime(30000);
        connector1.setRequestBufferSize(8192);
        
        _jetty.setConnectors(new Connector[]{ connector0, connector1});
 
        Context root = new Context(_jetty,"/latest",Context.SESSIONS);
        root.setResourceBase(_vmDataDir);
        root.addServlet(new ServletHolder(new VmDataServlet(this, USER_DATA)), "/user-data");
        root.addServlet(new ServletHolder(new VmDataServlet(this, META_DATA)), "/meta-data");
        
        ResourceHandler resource_handler = new ResourceHandler(); 
        resource_handler.setResourceBase("/var/lib/images/");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { root, resource_handler, new DefaultHandler() });
        _jetty.setHandler(handlers);
 
        _jetty.start();
        //_jetty.join();
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stop() {
        return true;
    }


    @Override
    public String getName() {
        return "JettyVmDataServer";
    }


    @Override
    public Answer handleVmDataCommand(VmDataCommand cmd) {
        String vmDataDir = _vmDataDir + File.separator + cmd.getVmName();
        try {
            _fs.cleanup(vmDataDir, _vmDataDir);
            _fs.mkdirs(vmDataDir);
        } catch (IOException e1) {
            s_logger.warn("Failed to cleanup vm data dir " + vmDataDir,  e1);
           return new Answer(cmd, false, "Failed to cleanup or create directory " + vmDataDir);
        }
       
        
        for (String [] item : cmd.getVmData()) {
            try {
                _fs.create(vmDataDir, item[1]);
                String vmDataFile = vmDataDir + File.separator + item[1];
                if (item[2] != null) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(vmDataFile));
                    writer.write(item[2]);
                    writer.close();
                }
            } catch (IOException e) {
                s_logger.warn("Failed to write vm data item " + item[1], e);
                return new Answer(cmd, false, "Failed to write vm data item " + item[1]);
            }
        }
        return new Answer(cmd);

    }


    @Override
    public void handleVmStarted(VirtualMachineTO vm) {
        for (NicTO nic: vm.getNics()) {
            if (nic.getType() == TrafficType.Guest) {
                if (nic.getIp() != null) {              
                    String ipv4File = _vmDataDir + File.separator + vm.getName() + File.separator + "local-ipv4";
                    try {
                        _fs.create(_vmDataDir + File.separator + vm.getName(), "local-ipv4");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(ipv4File));
                        writer.write(nic.getIp());
                        _ipVmMap.put(nic.getIp(), vm.getName());
                        writer.close();
                    } catch (IOException e) {
                        s_logger.warn("Failed to create or write to local-ipv4 file " + ipv4File);
                    }

                    
                }
                
            }
        }
    }


    @Override
    public void handleVmStopped(String vmName) {
        String vmDataDir = _vmDataDir + File.separator + vmName;
        try {
            _fs.cleanup(vmDataDir, _vmDataDir);
            _fs.mkdirs(vmDataDir);
        } catch (IOException e1) {
            s_logger.warn("Failed to cleanup vm data dir " + vmDataDir,  e1);
        }

    }

}
