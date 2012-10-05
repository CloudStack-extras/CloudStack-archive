package com.cloud.ucs.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.ResponseGenerator;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceManager;
import com.cloud.resource.ResourceService;
import com.cloud.ucs.database.UcsManagerDao;
import com.cloud.ucs.database.UcsManagerVO;
import com.cloud.utils.component.Inject;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchCriteria2;
import com.cloud.utils.db.SearchCriteriaService;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;

@Local(value = {UcsManager.class})
public class UcsManagerImpl implements UcsManager {
    public static final Logger s_logger = Logger.getLogger(UcsManagerImpl.class);
    
    @Inject
    private UcsManagerDao ucsDao;
    @Inject
    private ResourceService resourceService;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private ClusterDetailsDao clusterDetailsDao;

    private String cookie;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return "UcsManager";
    }

    @Override
    @DB
    public AddUcsManagerResponse addUcsManager(AddUcsManagerCmd cmd) {
        UcsManagerVO vo = new UcsManagerVO();
        vo.setUuid(UUID.randomUUID().toString());
        vo.setPassword(cmd.getPassword());
        vo.setUrl(cmd.getUrl());
        vo.setUsername(vo.getUsername());
        vo.setZoneId(cmd.getZoneId());
        vo.setName(cmd.getName());

        Transaction txn = Transaction.currentTxn();
        txn.start();
        ucsDao.persist(vo);
        txn.commit();
        AddUcsManagerResponse rsp = new AddUcsManagerResponse();
        rsp.setId(vo.getId());
        rsp.setName(vo.getName());
        rsp.setUrl(vo.getUrl());
        rsp.setZoneId(vo.getZoneId());
        return rsp;
    }

    @DB
    private void saveProfileToCluster(AddUcsClusterCmd cmd, Cluster cluster) {
        Map<String, String> details = new HashMap<String, String>(1);
        details.put(ApiConstants.UCS_PROFILE, cmd.getUcsProfile());
        Transaction txn = Transaction.currentTxn();
        txn.start();
        clusterDetailsDao.persist(cluster.getId(), details);
        txn.commit();
    }

    private String getUcsManagerIp() {
        SearchCriteriaService<UcsManagerVO, UcsManagerVO> serv = SearchCriteria2.create(UcsManagerVO.class);
        List<UcsManagerVO> vos = serv.list();
        if (vos.isEmpty()) {
            throw new CloudRuntimeException("Cannot find any UCS manager, you must add it first");
        }
        return vos.get(0).getUrl();
    }

    private String getUcsApiTemplate(String name) {
        try {
            InputStream is = this.getClass().getClassLoader().getSystemResourceAsStream(name);
            return IOUtils.toString(is);
        } catch (Exception e) {
            throw new CloudRuntimeException(String.format("Cannot find api template[%s]", name), e);
        }
    }

    private String getCookie() {
        try {
            if (cookie == null) {
                UcsClient client = new UcsClient(getUcsManagerIp());
                String login = getUcsApiTemplate("login.xml");
                String res = client.call(login);
                cookie = XmlFieldHelper.getField(res, "outCookie");
            }
            
            return cookie;
        } catch (Exception e) {
            throw new CloudRuntimeException("Cannot get cookie", e);
        }
    }

    private List<UcsBlade> listBlades() {
        cookie = getCookie();
        Map<String, String> tokens = new HashMap<String, String>(1);
        tokens.put("cookie", cookie);
        String cmd = XmlFieldHelper.replaceTokens(getUcsApiTemplate("listBlades.xml"), tokens);
        UcsClient client = new UcsClient(getUcsManagerIp());
        String res = client.call(cmd);
        return UcsBlade.valueOf(res);
    }
    
    private void discoverBladesToCluster(Cluster cluster) {
        for (UcsBlade b : listBlades()) {
            AddUcsHostCmd cmd = new AddUcsHostCmd();
            cmd.setAllocationState("Enabled");
            cmd.setClusterId(cluster.getId());
            cmd.setClusterName(cluster.getName());
            cmd.setHostTags(new ArrayList<String>(0));
            cmd.setHypervisor(HypervisorType.ManagedHost.toString());
            cmd.setPassword("");
            cmd.setPodId(cluster.getPodId());
            cmd.setUrl("");
            cmd.setUsername(b.getDn());
            cmd.setZoneId(cluster.getDataCenterId());
            cmd.execute();
        }
    }

    @Override
    public ListResponse<ClusterResponse> addUcsCluster(AddUcsClusterCmd cmd, ResponseGenerator responseGenerator) {
        try {
            List<? extends Cluster> result = resourceService.discoverCluster(cmd);
            ListResponse<ClusterResponse> response = new ListResponse<ClusterResponse>();
            List<ClusterResponse> clusterResponses = new ArrayList<ClusterResponse>();
            if (result != null) {
                for (Cluster cluster : result) {
                    saveProfileToCluster(cmd, cluster);
                    discoverBladesToCluster(cluster);
                    ClusterResponse clusterResponse = responseGenerator.createClusterResponse(cluster, false);
                    clusterResponses.add(clusterResponse);
                }
            } else {
                throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to add cluster");
            }
            response.setResponses(clusterResponses);
            return response;
        } catch (Exception e) {
            throw new CloudRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ListResponse<ListUcsProfileResponse> listUcsProfiles(ListUcsProfileCmd cmd) {
        cookie = getCookie();
        Map<String, String> tokens = new HashMap<String, String>(1);
        tokens.put("cookie", cookie);
        String ucsCmd = XmlFieldHelper.replaceTokens(getUcsApiTemplate("listProfiles.xml"), tokens);
        UcsClient client = new UcsClient(getUcsManagerIp());
        String res = client.call(ucsCmd);
        List<UcsProfile> profiles = UcsProfile.valueOf(res);
        ListResponse<ListUcsProfileResponse> response = new ListResponse<ListUcsProfileResponse>();
        List<ListUcsProfileResponse> rs = new ArrayList<ListUcsProfileResponse>();
        for (UcsProfile p : profiles) {
            ListUcsProfileResponse r = new ListUcsProfileResponse();
            r.setDn(p.getDn());
            rs.add(r);
        }
        response.setResponses(rs);
        return response;
    }

    @Override
    public void associateProfileToBladesInCluster(AssociateUcsProfileToBladesInClusterCmd cmd) throws InterruptedException {
        Map<String, String> details = clusterDetailsDao.findDetails(cmd.getClusterId());
        String profileDn = details.get(ApiConstants.UCS_PROFILE);
        assert profileDn != null;
        SearchCriteriaService<HostVO, HostVO> serv = SearchCriteria2.create(HostVO.class);
        serv.addAnd(serv.getEntity().getClusterId(), Op.EQ, cmd.getClusterId());
        List<HostVO> hosts = serv.list();
        Map<String, Boolean> status = new HashMap<String, Boolean>(hosts.size());
        UcsClient client = new UcsClient(getUcsManagerIp());
        cookie = getCookie();
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("cookie", cookie);
        for (HostVO h : hosts) {
            String ucsCmd = XmlFieldHelper.replaceTokens(getUcsApiTemplate("AssociateProfile.xml"), tokens);
            tokens.put("pdn", profileDn);
            tokens.put("cdn", h.getName());
            client.call(ucsCmd);
            status.put(h.getName(), false);
        }
        
        tokens = new HashMap<String, String>();
        tokens.put("cookie", cookie);
        while (true) {
            for (Map.Entry<String, Boolean> e : status.entrySet()) {
                tokens.put("dn", e.getKey());
                String ucsCmd = XmlFieldHelper.replaceTokens(getUcsApiTemplate("getBladeStatus.xml"), tokens);
                String res = client.call(ucsCmd);
                String val = XmlFieldHelper.getField(res, "association");
                if (val.equals("associated")) {
                    e.setValue(true);
                }
            }
            
            boolean isAllAssociated = true;
            for (Map.Entry<String, Boolean> e : status.entrySet()) {
                if (!e.getValue()) {
                    isAllAssociated = false;
                    break;
                }
            }
            
            if (isAllAssociated) {
                break;
            }
            
            TimeUnit.SECONDS.sleep(2);
        }
        
        s_logger.debug(String.format("all blades in cluster[%s] are associated", cmd.getClusterId()));
    }
}
