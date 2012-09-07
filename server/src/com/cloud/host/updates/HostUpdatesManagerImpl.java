package com.cloud.host.updates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.HostUpdatesAnswer;
import com.cloud.agent.api.HostUpdatesCommand;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.host.updates.dao.HostUpdatesDao;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;

@Local(value={HostUpdatesManager.class})
public class HostUpdatesManagerImpl extends Xensdk implements HostUpdatesManager { 
	
	@Inject
	protected AgentManager _agentMgr;
	@Inject
	protected HostDao _hostDao;
	@Inject
	protected HostDetailsDao _hostDetailsDao;
	@Inject
	protected HostUpdatesDao _hostUpdatesDao;
	private String _name;
	private static final Logger s_logger = Logger.getLogger(HostUpdatesManagerImpl.class.getName());
	private Timer _timer = null;
	
	public final static String HYPERVISOR_TYPE = "XenServer";
	public final static long HEARTBEAT_INTERVAL = 10000;
	
	HostUpdatesCommand cmd = null;
	
	

    private Document getHostVersionsFile(){

    	File updatesFile  = null;
		try {
			updatesFile = File.createTempFile("host_updates", ".xml");
		} catch (IOException e) {
			s_logger.debug("Cannot create a temp file.", e);
		}
        URL updates = null;
		try {
			updates = new URL("http://updates.xensource.com/XenServer/updates.xml");
		} catch (MalformedURLException e) {
			s_logger.debug("Cannot download the file from Internet link.", e);
		}
        ReadableByteChannel fis = null;
		try {
			fis = Channels.newChannel(updates.openStream());
		} catch (IOException e) {
			s_logger.debug("Cannot open a channel for IO", e);
		}
        FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(updatesFile);
		} catch (FileNotFoundException e) {
			s_logger.debug("Cannot open Output stream.", e);
		}
        try {
			fos.getChannel().transferFrom(fis, 0, 1 << 24);
		} catch (IOException e) {
			s_logger.debug("Not able to transfer data to temp file.", e);
		}

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			s_logger.debug("Cannot create new Document Builder.", e);
		}
        Document doc = null;
		try {
			doc = docBuilder.parse(updatesFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        doc.getDocumentElement().normalize();
        updatesFile.delete();
        
		return doc;       
    }
    
    private List<String> getreleasedPatches(Element serverVersionsNode,String hypervisorVersion){
    	Element version = null;
    	List<String> releasedPatches = new ArrayList<String>();
    	NodeList versionList = serverVersionsNode.getElementsByTagName("version");
    	for(int counter = 0; counter < versionList.getLength(); counter++)
        {
        	Node nNode = versionList.item(counter);
        	version = (Element) nNode;
        	NodeList patchList = null;
			if(version.getAttribute("value").equals(hypervisorVersion))
        		{
					patchList = version.getElementsByTagName("patch");
	        		for(int temp = 0; temp < patchList.getLength(); temp++)
	        		{
	        			Node patchNode = patchList.item(temp);
	        			Element patchElement = (Element) patchNode;
	        			releasedPatches.add(patchElement.getAttribute("uuid"));
	        		}
	        		break;
        		}
        }
    	return releasedPatches;
    }
    
    private void fillUpdates(Element patchDetailsNode, List<String> releasedPatches, long hostId){
    	NodeList patchDetails = patchDetailsNode.getElementsByTagName("patch");
		for(int patchCounter = 0; patchCounter < releasedPatches.size(); patchCounter++)
        {
			for(int counter = 0; counter < patchDetails.getLength(); counter++)
	        {
	        	Node patchNode = patchDetails.item(counter);
	        	Element patchInfo = (Element) patchNode;
	        	if(patchInfo.getAttribute("uuid").equals(releasedPatches.get(patchCounter)))
	        	{
	        		String patchId = releasedPatches.get(patchCounter);
	        		HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
	        		if(patch == null)
	        		{
	        			//insert new update
	        			HostUpdatesVO newUpdate = new HostUpdatesVO();
	        			newUpdate.setUuid(patchInfo.getAttribute("uuid"));
	        			newUpdate.setLable(patchInfo.getAttribute("name-label"));
	        			newUpdate.setDescription(patchInfo.getAttribute("name-description"));
	        			newUpdate.setHostId(hostId);
	        			newUpdate.setAfterApplyGuidance(patchInfo.getAttribute("after-apply-guidance"));
	        			newUpdate.setURL(patchInfo.getAttribute("url"));
	        			newUpdate.setUpdateApplied(false);
	        			newUpdate.setTimestamp(patchInfo.getAttribute("timestamp"));
	        			_hostUpdatesDao.persist(newUpdate);
	        		}
	        	}
	        }
        }
    }
    
    @DB
    public void updateAppliedField(List<String> appliedPatchList){
		Transaction txn = Transaction.currentTxn();
    	txn.start();
    	for(int counter = 0; counter < appliedPatchList.size(); counter++)
		{
			String patchId = appliedPatchList.get(counter);
    		HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
    		if(patch != null)
    		{
    			HostUpdatesVO update = _hostUpdatesDao.lockRow(patch.getId(), true);
    			update.setUpdateApplied(true);
    			_hostUpdatesDao.update(patch.getId(), update);
    			txn.commit();
    		}
		}
		txn.close();
    }

	
	private TimerTask checkHostUpdates() {
		return new TimerTask() {
			
			@Override
			public void run() {
				
				try {
					Document doc = null;
					doc = getHostVersionsFile(); 
					List<HostVO> hosts = _hostDao.listAll();
					if(hosts != null)
					for(HostVO host : hosts)
					{
						if(host.getHypervisorType() != null)
							if(host.getHypervisorType().name().equals(HYPERVISOR_TYPE))
							{
								long hostId = host.getId();
								String hypervisorVersion = _hostDetailsDao.findDetail(hostId, "product_version").getValue();
								Element serverVersionsNode = (Element) doc.getElementsByTagName("serverversions").item(0);
								List<String> releasedPatches = getreleasedPatches(serverVersionsNode, hypervisorVersion);
								
								Element patchDetailsNode = (Element) doc.getElementsByTagName("patches").item(0);
								fillUpdates(patchDetailsNode, releasedPatches, hostId);

								cmd = new HostUpdatesCommand();
								HostUpdatesAnswer updates = (HostUpdatesAnswer) _agentMgr.send(hostId, cmd);
								List<String> appliedPatchList = updates.getAppliedPatchList();
								updateAppliedField(appliedPatchList);
								
								//System.out.println(updates.getDetails());
							}
					}
				} catch (Throwable e){s_logger.error("Exception in CapacityChecker", e);};
			}
		};
	}
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		_name = name;

        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        ConfigurationDao configDao = locator.getDao(ConfigurationDao.class);
        Map<String, String> configs = configDao.getConfiguration(params);
        _timer = new Timer();
		return false;
	}
	
	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public boolean start() {
		_timer.schedule(checkHostUpdates(), HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
		return true;
	}
	
	@Override
	public boolean stop() {
		_timer.cancel();
		return false;
	}
}
