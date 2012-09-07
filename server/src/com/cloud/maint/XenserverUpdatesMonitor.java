package com.cloud.maint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.HostUpdatesAnswer;
import com.cloud.agent.api.HostUpdatesCommand;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.host.updates.HostUpdatesRefVO;
import com.cloud.host.updates.HostUpdatesVO;
import com.cloud.host.updates.dao.HostUpdatesDao;
import com.cloud.host.updates.dao.HostUpdatesRefDao;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria2;
import com.cloud.utils.db.SearchCriteriaService;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.SearchCriteria.Op;

@Local(value = HostUpdatesMonitor.class)
public class XenserverUpdatesMonitor implements HostUpdatesMonitor {

    @Inject
    ConfigurationDao                  _configDao;
    @Inject
    HostUpdatesRefDao                 _hostUpdatesRefDao;
    @Inject
    HostUpdatesDao                    _hostUpdatesDao;
    @Inject
    HostDetailsDao                    _hostDetailsDao;
    @Inject
    AgentManager                      _agentMgr;
    String _url;
    Map<String, String> _configs;
    private String _name;
    public final static String HYPERVISOR_TYPE = "XenServer";
    private static final Logger s_logger = Logger.getLogger(XenserverUpdatesMonitor.class);

    public Document getHostVersionsFile(String url) {
        File updatesFile  = null;
        try {
            updatesFile = File.createTempFile("host_updates", ".xml");
        } catch (IOException e) {
            s_logger.debug("Cannot create a temp file.", e);
        }
        URL updates = null;
        try {
            if( url == null) {
                updates = new URL("http://updates.xensource.com/XenServer/updates.xml");
            }
            else {
                updates = new URL(url);
            }
        } catch (MalformedURLException e) {
            s_logger.debug("Cannot download the file from Internet link " , e);
        }
        ReadableByteChannel fis = null;
        try {
            fis = Channels.newChannel(updates.openStream());
        } catch (IOException e) {
            s_logger.debug("Cannot open a channel for IO, looks like the URL is not valid : " + url);
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

    public List<String> getReleasedPatches(Element serverVersionsNode,String hypervisorVersion) {
        Element version = null;
        List<String> releasedPatches = new ArrayList<String>();
        NodeList versionList = serverVersionsNode.getElementsByTagName("version");
        for(int counter = 0; counter < versionList.getLength(); counter++) {
            Node nNode = versionList.item(counter);
            version = (Element) nNode;
            NodeList patchList = null;
            if(version.getAttribute("value").equals(hypervisorVersion)) {
                patchList = version.getElementsByTagName("patch");
                for(int temp = 0; temp < patchList.getLength(); temp++) {
                    Node patchNode = patchList.item(temp);
                    Element patchElement = (Element) patchNode;
                    releasedPatches.add(patchElement.getAttribute("uuid"));
                }
                break;
            }
        }
        return releasedPatches;
    }

    public void fillUpdates(Element patchDetailsNode, List<String> releasedPatches, long hostId) {
        NodeList patchDetails = patchDetailsNode.getElementsByTagName("patch");
        for(int patchCounter = 0; patchCounter < releasedPatches.size(); patchCounter++) {
            for(int counter = 0; counter < patchDetails.getLength(); counter++) {
                Node patchNode = patchDetails.item(counter);
                Element patchInfo = (Element) patchNode;
                if(patchInfo.getAttribute("uuid").equals(releasedPatches.get(patchCounter))) {
                    String patchId = releasedPatches.get(patchCounter);
                    HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
                    if(patch == null) {
                        //insert new patch info into host_updates table 
                        HostUpdatesVO newUpdate = new HostUpdatesVO();
                        newUpdate.setUuid(patchInfo.getAttribute("uuid"));
                        newUpdate.setLable(patchInfo.getAttribute("name-label"));
                        newUpdate.setDescription(patchInfo.getAttribute("name-description"));
                        newUpdate.setAfterApplyGuidance(patchInfo.getAttribute("after-apply-guidance"));
                        newUpdate.setURL(patchInfo.getAttribute("url"));
                        newUpdate.setTimestamp(patchInfo.getAttribute("timestamp"));
                        _hostUpdatesDao.persist(newUpdate);
                    }
                    patch = _hostUpdatesDao.findByUUID(patchId);

                    //insert entry into host_updates_ref table 
                    if( _hostUpdatesRefDao.findUpdate(hostId, patch.getId()) == null) {
                        HostUpdatesRefVO newEntry = new HostUpdatesRefVO();
                        newEntry.setHostId(hostId);
                        newEntry.setPatchId(patch.getId());
                        newEntry.setIsApplied(false);
                        _hostUpdatesRefDao.persist(newEntry);
                    }
                }
            }
        }
    }

    @DB
    public void updateAppliedField(List<String> appliedPatchList, long hostId) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        for(int counter = 0; counter < appliedPatchList.size(); counter++) {
            String patchId = appliedPatchList.get(counter);
            HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
            HostUpdatesRefVO hostUpdatesRef = null;
            if(patch != null) {
                hostUpdatesRef  = _hostUpdatesRefDao.findUpdate(hostId, patch.getId());
            }

            if(hostUpdatesRef != null && !hostUpdatesRef.getIsApplied()) {
                HostUpdatesRefVO patchRef = _hostUpdatesRefDao.lockRow(hostUpdatesRef.getId(), true);
                patchRef.setIsApplied(true);
                _hostUpdatesRefDao.update(hostUpdatesRef.getId(), patchRef);
                txn.commit();
            }
        }
        txn.close();
    }

    @Override
    public void updateHosts(long serverId) {
        Document doc = null; 
        HostUpdatesCommand cmd = null;


        SearchCriteriaService<HostVO, HostVO> sc = SearchCriteria2.create(HostVO.class);
        sc.addAnd(sc.getEntity().getManagementServerId(), Op.EQ, serverId);
        sc.addAnd(sc.getEntity().getType(), Op.EQ, Host.Type.Routing);
        sc.addAnd(sc.getEntity().getHypervisorType(), Op.EQ, HYPERVISOR_TYPE);
        List<HostVO> hosts = sc.list();
        try {
            if(hosts != null) {
                for(HostVO host : hosts) {
                    long hostId = host.getId();
                    if(doc == null) {
                        doc = getHostVersionsFile(_url);
                    }
                    String hypervisorVersion = _hostDetailsDao.findDetail(hostId, "product_version").getValue();
                    Element serverVersionsNode = (Element) doc.getElementsByTagName("serverversions").item(0);
                    List<String> releasedPatches = getReleasedPatches(serverVersionsNode, hypervisorVersion);

                    Element patchDetailsNode = (Element) doc.getElementsByTagName("patches").item(0);
                    fillUpdates(patchDetailsNode, releasedPatches, hostId);

                    cmd = new HostUpdatesCommand();
                    HostUpdatesAnswer updates = (HostUpdatesAnswer) _agentMgr.send(hostId, cmd);
                    List<String> appliedPatchList = updates.getAppliedPatchList();
                    updateAppliedField(appliedPatchList, hostId);
                }
            }
        } catch(Throwable e){
            if(doc == null) {
                s_logger.error("Exception in Host Update Checker:  Pass the correct update location URL" +  e);
            }
        }
    }

    public XenserverUpdatesMonitor() {
        super();
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
    throws ConfigurationException {
        _configs = _configDao.getConfiguration("management-server", params);
        _url = _configs.get(Config.XenUpdateURL.key());
        return true;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return _name;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

}
