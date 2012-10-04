package com.cloud.maint;

import java.io.IOException;
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
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.host.updates.HostUpdatesRefVO;
import com.cloud.host.updates.HostUpdatesVO;
import com.cloud.host.updates.dao.HostUpdatesDao;
import com.cloud.host.updates.dao.HostUpdatesRefDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ManagementServer;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;

@Local(value = HostUpdatesMonitor.class)
public class XenServerUpdatesMonitor implements HostUpdatesMonitor {

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
    @Inject
    ResourceManager                   _resourceMgr;
    String _url;
    Map<String, String> _configs;
    Long _serverId;
    private String _name;
    private static final Logger s_logger = Logger.getLogger(XenServerUpdatesMonitor.class);

    private Document getHostVersionsFile() {
        Document doc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(_url);
        } catch (IOException e) {
            s_logger.debug("Cannot able to find input or output stream file.", e);
        }
        catch (SAXException e) {
            s_logger.debug("Not able to parse the document from xml file " + e);
        }
        catch (ParserConfigurationException e) {
            s_logger.debug("Cannot create new Document Builder.", e);
        }
        return doc;
    }

    private List<String> getReleasedPatches(Element serverVersionsNode,String hypervisorVersion) {
        Element version = null;
        List<String> releasedPatches = new ArrayList<String>();
        NodeList versionList = serverVersionsNode.getElementsByTagName("version");
        for (int counter = 0; counter < versionList.getLength(); counter++) {
            Node nNode = versionList.item(counter);
            version = (Element) nNode;
            NodeList patchList = null;
            if (version.getAttribute("value").equals(hypervisorVersion)) {
                patchList = version.getElementsByTagName("patch");
                for (int temp = 0; temp < patchList.getLength(); temp++) {
                    Node patchNode = patchList.item(temp);
                    Element patchElement = (Element) patchNode;
                    releasedPatches.add(patchElement.getAttribute("uuid"));
                }
                break;
            }
        }
        return releasedPatches;
    }

    private void fillUpdates(Element patchDetailsNode, List<String> releasedPatches, long hostId) {
        NodeList patchDetails = patchDetailsNode.getElementsByTagName("patch");
        for (int patchCounter = 0; patchCounter < releasedPatches.size(); patchCounter++) {
            for (int counter = 0; counter < patchDetails.getLength(); counter++) {
                Node patchNode = patchDetails.item(counter);
                Element patchInfo = (Element) patchNode;
                if (patchInfo.getAttribute("uuid").equals(releasedPatches.get(patchCounter))) {
                    String patchId = releasedPatches.get(patchCounter);
                    HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
                    if (patch == null) {
                        //insert new patch info into host_updates table 
                        HostUpdatesVO newUpdate = new HostUpdatesVO();
                        newUpdate.setUuid(patchInfo.getAttribute("uuid"));
                        newUpdate.setLable(patchInfo.getAttribute("name-label"));
                        newUpdate.setDescription(patchInfo.getAttribute("name-description"));
                        newUpdate.setAfterApplyGuidance(patchInfo.getAttribute("after-apply-guidance"));
                        newUpdate.setURL(patchInfo.getAttribute("url"));
                        newUpdate.setTimestamp(patchInfo.getAttribute("timestamp"));
                        _hostUpdatesDao.persist(newUpdate);
                        patch = _hostUpdatesDao.findByUUID(patchId);
                    }

                    //insert entry into host_updates_ref table 
                    if ( _hostUpdatesRefDao.findUpdate(hostId, patch.getId()) == null) {
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
    private void updateAppliedField(List<String> appliedPatchList, long hostId) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        for (int counter = 0; counter < appliedPatchList.size(); counter++) {
            String patchId = appliedPatchList.get(counter);
            HostUpdatesVO patch = _hostUpdatesDao.findByUUID(patchId);
            HostUpdatesRefVO hostUpdatesRef = null;
            if (patch != null) {
                hostUpdatesRef  = _hostUpdatesRefDao.findUpdate(hostId, patch.getId());
            }

            if (hostUpdatesRef != null && !hostUpdatesRef.getIsApplied()) {
                HostUpdatesRefVO patchRef = _hostUpdatesRefDao.lockRow(hostUpdatesRef.getId(), true);
                patchRef.setIsApplied(true);
                _hostUpdatesRefDao.update(hostUpdatesRef.getId(), patchRef);
                txn.commit();
            }
        }
        txn.close();
    }

    @Override
    public void updateHosts() {
        Document doc = null; 
        HostUpdatesCommand cmd = null;
        List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHostsByHypervisor(HypervisorType.XenServer, _serverId);
        try {
            if (hosts != null && !hosts.isEmpty()) {
                doc = getHostVersionsFile();
                if (doc != null) {
                    for (HostVO host : hosts) {
                        long hostId = host.getId();
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
                } else {
                    s_logger.error("Couldn't download the xen updates file from " + _url);
                }
            }
        } catch (Exception e) {
            if (doc == null) {
                s_logger.error("Exception in Host Update Checker:  Pass the correct update location URL" +  e);
            }
        }
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _configs = _configDao.getConfiguration("management-server", params);
        _url = _configs.get(Config.XenUpdateURL.key());
        _serverId = ((ManagementServer) ComponentLocator.getComponent(ManagementServer.Name)).getId();
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
