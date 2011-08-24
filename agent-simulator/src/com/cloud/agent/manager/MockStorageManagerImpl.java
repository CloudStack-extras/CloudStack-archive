/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.agent.manager;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.AttachVolumeAnswer;
import com.cloud.agent.api.AttachVolumeCommand;
import com.cloud.agent.api.BackupSnapshotAnswer;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.ComputeChecksumCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.agent.api.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.CreateVolumeFromSnapshotAnswer;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.ManageSnapshotAnswer;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.ModifyStoragePoolAnswer;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.SecStorageSetupAnswer;
import com.cloud.agent.api.SecStorageSetupCommand;
import com.cloud.agent.api.SecStorageVMSetupCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadAnswer;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.simulator.MockHost;
import com.cloud.simulator.MockSecStorageVO;
import com.cloud.simulator.MockStoragePoolVO;
import com.cloud.simulator.MockVMVO;
import com.cloud.simulator.MockVolumeVO;
import com.cloud.simulator.MockVolumeVO.MockVolumeType;
import com.cloud.simulator.dao.MockHostDao;
import com.cloud.simulator.dao.MockSecStorageDao;
import com.cloud.simulator.dao.MockStoragePoolDao;
import com.cloud.simulator.dao.MockVMDao;
import com.cloud.simulator.dao.MockVolumeDao;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.component.Inject;
import com.cloud.vm.DiskProfile;


@Local(value = { MockStorageManager.class })
public class MockStorageManagerImpl implements MockStorageManager {
    private static final Logger s_logger = Logger.getLogger(MockStorageManagerImpl.class);
    @Inject MockStoragePoolDao _mockStoragePoolDao = null;
    @Inject MockSecStorageDao _mockSecStorageDao = null;
    @Inject MockVolumeDao _mockVolumeDao = null;
    @Inject MockVMDao _mockVMDao = null;
    @Inject MockHostDao _mockHostDao = null;

    private MockVolumeVO findVolumeFromSecondary(String path, String ssUrl, MockVolumeType type) {
       
        String volumePath = path.replaceAll(ssUrl, "");
        
        MockSecStorageVO secStorage = _mockSecStorageDao.findByUrl(ssUrl);
        if (secStorage == null) {
            return null;
        }
        
        volumePath = secStorage.getMountPoint() + volumePath;
        volumePath = volumePath.replaceAll("//", "/");
        
        MockVolumeVO volume = _mockVolumeDao.findByStoragePathAndType(volumePath);
        if (volume == null) {
            return null;
        }
        
        return volume;
    }
    @Override
    public PrimaryStorageDownloadAnswer primaryStorageDownload(PrimaryStorageDownloadCommand cmd) {
        MockVolumeVO template = findVolumeFromSecondary(cmd.getUrl(),cmd.getSecondaryStorageUrl(), MockVolumeType.TEMPLATE);
        if (template == null) {
            return new PrimaryStorageDownloadAnswer("Can't find primary storage");
        }
        
        MockStoragePoolVO primaryStorage = _mockStoragePoolDao.findByUuid(cmd.getPoolUuid());
        if (primaryStorage == null) {
            return new PrimaryStorageDownloadAnswer("Can't find primary storage"); 
        }
        
        String volumeName = UUID.randomUUID().toString();
        MockVolumeVO newVolume = new MockVolumeVO();
        newVolume.setName(volumeName);
        newVolume.setPath(primaryStorage.getMountPoint() + volumeName);
        newVolume.setPoolId(primaryStorage.getId());
        newVolume.setSize(template.getSize());
        newVolume.setType(MockVolumeType.VOLUME);
        _mockVolumeDao.persist(newVolume);
        
       
        return new PrimaryStorageDownloadAnswer(newVolume.getPath(), newVolume.getSize());
    }

    @Override
    public CreateAnswer createVolume(CreateCommand cmd) {
        StorageFilerTO sf = cmd.getPool();
        DiskProfile dskch = cmd.getDiskCharacteristics();
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findByUuid(sf.getUuid());
        if (storagePool == null) {
            return new CreateAnswer(cmd, "Failed to find storage pool: " + sf.getUuid());
        }
        
        String volumeName = UUID.randomUUID().toString();
        MockVolumeVO volume = new MockVolumeVO();
        volume.setPoolId(storagePool.getId());
        volume.setName(volumeName);
        volume.setPath(storagePool.getMountPoint() + volumeName);
        volume.setSize(dskch.getSize());
        volume.setType(MockVolumeType.VOLUME);
        volume = _mockVolumeDao.persist(volume);
        
        VolumeTO volumeTo = new VolumeTO(cmd.getVolumeId(), dskch.getType(), sf.getType(), sf.getUuid(), 
                volume.getName(), storagePool.getMountPoint(), volume.getPath(), volume.getSize(), null);
        
        return new CreateAnswer(cmd, volumeTo);
    }

    @Override
    public AttachVolumeAnswer AttachVolume(AttachVolumeCommand cmd) {
        String poolid = cmd.getPoolUuid();
        String volumeName = cmd.getVolumeName();
        MockVolumeVO volume = _mockVolumeDao.findByStoragePathAndType(cmd.getVolumePath());
        if (volume == null) {
            return new AttachVolumeAnswer(cmd, "Can't find volume:" + volumeName + "on pool:" + poolid);
        }
        
        String vmName = cmd.getVmName();
        MockVMVO vm = _mockVMDao.findByVmName(vmName);
        if (vm == null) {
            return new AttachVolumeAnswer(cmd, "can't vm :" + vmName);
        }
        
        return new AttachVolumeAnswer(cmd, cmd.getDeviceId());
    }

    @Override
    public Answer AttachIso(AttachIsoCommand cmd) {
       MockVolumeVO iso = findVolumeFromSecondary(cmd.getIsoPath(), cmd.getStoreUrl(), MockVolumeType.ISO);
       if (iso == null) {
           return new Answer(cmd, false, "Failed to find the iso: " + cmd.getIsoPath() + "on secondary storage " + cmd.getStoreUrl());
       }
       
       String vmName = cmd.getVmName();
       MockVMVO vm = _mockVMDao.findByVmName(vmName);
       if (vm == null) {
           return new Answer(cmd, false, "can't vm :" + vmName);
       }
       
       return new Answer(cmd);
    }

    @Override
    public Answer DeleteStoragePool(DeleteStoragePoolCommand cmd) {
        MockStoragePoolVO storage = _mockStoragePoolDao.findByUuid(cmd.getPool().getUuid());
        if (storage == null) {
            return new Answer(cmd, false, "can't find storage pool:" + cmd.getPool().getUuid());
        }
        _mockStoragePoolDao.remove(storage.getId());
        return new Answer(cmd);
    }

    @Override
    public ModifyStoragePoolAnswer ModifyStoragePool(ModifyStoragePoolCommand cmd) {
        StorageFilerTO sf = cmd.getPool();
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findByUuid(sf.getUuid());
        if (storagePool == null) {
            storagePool = new MockStoragePoolVO();
            storagePool.setUuid(sf.getUuid());
            storagePool.setMountPoint("/mnt/" + sf.getUuid() + File.separator);
            
            Long size = DEFAULT_HOST_STORAGE_SIZE;
            String path = sf.getPath();
            int index = path.lastIndexOf("/");
            if (index != -1) {
                path = path.substring(index+1);
                if (path != null) {
                    String values[] =  path.split("=");
                    if (values.length > 1 && values[0].equalsIgnoreCase("size")) {
                        size = Long.parseLong(values[1]);
                    }
                }
            }
           
            storagePool.setCapacity(size);
         
            storagePool.setStorageType(sf.getType());
            storagePool =  _mockStoragePoolDao.persist(storagePool);
        }

        return new ModifyStoragePoolAnswer(cmd, storagePool.getCapacity(), 0, new HashMap<String, TemplateInfo>());
    }

    @Override
    public Answer CreateStoragePool(CreateStoragePoolCommand cmd) {
        StorageFilerTO sf = cmd.getPool();
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findByUuid(sf.getUuid());
        if (storagePool == null) {
            storagePool = new MockStoragePoolVO();
            storagePool.setUuid(sf.getUuid());
            storagePool.setMountPoint("/mnt/" + sf.getUuid() + File.separator);

            Long size = DEFAULT_HOST_STORAGE_SIZE;
            String path = sf.getPath();
            int index = path.lastIndexOf("/");
            if (index != -1) {
                path = path.substring(index+1);
                if (path != null) {
                    String values[] =  path.split("=");
                    if (values.length > 1 && values[0].equalsIgnoreCase("size")) {
                        size = Long.parseLong(values[1]);
                    }
                }
            }
            storagePool.setCapacity(size);
         
            storagePool.setStorageType(sf.getType());
            storagePool =  _mockStoragePoolDao.persist(storagePool);
        }

        return new ModifyStoragePoolAnswer(cmd, storagePool.getCapacity(), 0, new HashMap<String, TemplateInfo>());
    }

    @Override
    public Answer SecStorageSetup(SecStorageSetupCommand cmd) {
        MockSecStorageVO storage = _mockSecStorageDao.findByUrl(cmd.getSecUrl());
        if (storage == null) {
            return new Answer(cmd, false, "can't find the storage");
        }
        return new SecStorageSetupAnswer(storage.getMountPoint());
    }

    @Override
    public Answer ListTemplates(ListTemplateCommand cmd) {
        MockSecStorageVO storage = _mockSecStorageDao.findByUrl(cmd.getSecUrl());
        if (storage == null) {
            return new Answer(cmd, false, "Failed to get secondary storage");
        }
        
        List<MockVolumeVO> templates = _mockVolumeDao.findByStorageIdAndType(storage.getId(), MockVolumeType.TEMPLATE);
        Map<String, TemplateInfo> templateInfos = new HashMap<String, TemplateInfo>();
        for (MockVolumeVO template : templates) {
            templateInfos.put(template.getName(), new TemplateInfo(template.getName(), template.getPath().replaceAll(storage.getMountPoint(), ""), template.getSize(), template.getSize(), true, false));
        }
        
        return new ListTemplateAnswer(cmd.getSecUrl(), templateInfos);
        
    }

    @Override
    public Answer Destroy(DestroyCommand cmd) {

        MockVolumeVO volume = _mockVolumeDao.findByStoragePathAndType(cmd.getVolume().getPath());
        if (volume != null) {
            _mockVolumeDao.remove(volume.getId());
        }
        return new Answer(cmd);
    }

    @Override
    public DownloadAnswer Download(DownloadCommand cmd) {
        MockSecStorageVO ssvo = _mockSecStorageDao.findByUrl(cmd.getSecUrl());
        if (ssvo == null) {
            return new DownloadAnswer("can't find secondary storage", VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
        }
        
        MockVolumeVO volume = new MockVolumeVO();
        volume.setPoolId(ssvo.getId());
        volume.setName(cmd.getName());
        volume.setPath(ssvo.getMountPoint() + cmd.getName());
        volume.setSize(0);
        volume.setType(MockVolumeType.TEMPLATE);
        volume.setStatus(Status.DOWNLOAD_IN_PROGRESS);
        volume = _mockVolumeDao.persist(volume);
       
        return new DownloadAnswer(String.valueOf(volume.getId()), 0, "Downloading", Status.DOWNLOAD_IN_PROGRESS, cmd.getName(), cmd.getName(), volume.getSize(), volume.getSize());
    }

    @Override
    public DownloadAnswer DownloadProcess(DownloadProgressCommand cmd) {
        String volumeId = cmd.getJobId();
        MockVolumeVO volume = _mockVolumeDao.findById(Long.parseLong(volumeId));
        if (volume == null) {
            return new DownloadAnswer("Can't find the downloading volume", Status.ABANDONED);
        }
        
        long size = Math.min(volume.getSize() + DEFAULT_TEMPLATE_SIZE/5, DEFAULT_TEMPLATE_SIZE);
        volume.setSize(size);
       
        double volumeSize = volume.getSize();
        double pct = volumeSize/DEFAULT_TEMPLATE_SIZE;
        if (pct >= 1.0) {
            volume.setStatus(Status.DOWNLOADED);
            _mockVolumeDao.update(volume.getId(), volume);
            return new DownloadAnswer(cmd.getJobId(), 100, cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED, volume.getPath(), volume.getName());
        } else {
            _mockVolumeDao.update(volume.getId(), volume);
            return new DownloadAnswer(cmd.getJobId(), (int)(pct*100.0) , cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOAD_IN_PROGRESS, volume.getPath(), volume.getName()); 
        }
    }

    @Override
    public GetStorageStatsAnswer GetStorageStats(GetStorageStatsCommand cmd) {
        String uuid = cmd.getStorageId();
        if (uuid == null) {
            String secUrl = cmd.getSecUrl();
            MockSecStorageVO secondary = _mockSecStorageDao.findByUrl(secUrl);
            if (secondary == null) {
                return new GetStorageStatsAnswer(cmd, "Can't find the secondary storage:" + secUrl);
            }
            Long totalUsed = _mockVolumeDao.findTotalStorageId(secondary.getId());
            return new GetStorageStatsAnswer(cmd, secondary.getCapacity(), totalUsed);
        } else {
            MockStoragePoolVO pool = _mockStoragePoolDao.findByUuid(uuid);
            if (pool == null) {
                return new GetStorageStatsAnswer(cmd, "Can't find the pool");
            }
            Long totalUsed = _mockVolumeDao.findTotalStorageId(pool.getId());
            if (totalUsed == null) {
                totalUsed = 0L;
            }
            return new GetStorageStatsAnswer(cmd, pool.getCapacity(), totalUsed);
        }
    }

    @Override
    public ManageSnapshotAnswer ManageSnapshot(ManageSnapshotCommand cmd) {
        String volPath = cmd.getVolumePath();
        
        MockVolumeVO volume = _mockVolumeDao.findByStoragePathAndType(volPath);
        if (volume == null) {
            return new ManageSnapshotAnswer(cmd, false, "Can't find the volume");
        }
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findById(volume.getPoolId());
        if (storagePool == null) {
            return new ManageSnapshotAnswer(cmd, false, "Can't find the storage pooll"); 
        }
        
        String mountPoint = storagePool.getMountPoint();
        MockVolumeVO snapshot = new MockVolumeVO();
        
        snapshot.setName(cmd.getSnapshotName());
        snapshot.setPath(mountPoint + cmd.getSnapshotName());
        snapshot.setSize(volume.getSize());
        snapshot.setPoolId(storagePool.getId());
        snapshot.setType(MockVolumeType.SNAPSHOT);
        snapshot.setStatus(Status.DOWNLOADED);
        
        snapshot = _mockVolumeDao.persist(snapshot);
        
        return new ManageSnapshotAnswer(cmd, snapshot.getId(), snapshot.getPath(), true, "");
    }

    @Override
    public BackupSnapshotAnswer BackupSnapshot(BackupSnapshotCommand cmd) {
        String snapshotPath = cmd.getSnapshotUuid();
        MockVolumeVO snapshot = _mockVolumeDao.findByStoragePathAndType(snapshotPath);
        if (snapshot == null) {
            return new BackupSnapshotAnswer(cmd, false, "can't find snapshot" + snapshotPath, null, true);
        }
        
        String secStorageUrl = cmd.getSecondaryStoragePoolURL();
        MockSecStorageVO secStorage = _mockSecStorageDao.findByUrl(secStorageUrl);
        if (secStorage == null) {
            return new BackupSnapshotAnswer(cmd, false, "can't find sec storage" + snapshotPath, null, true);
        }
        MockVolumeVO newsnapshot = new MockVolumeVO();
        String name = UUID.randomUUID().toString();
        newsnapshot.setName(name);
        newsnapshot.setPath(secStorage.getMountPoint() + name);
        newsnapshot.setPoolId(secStorage.getId());
        newsnapshot.setSize(snapshot.getSize());
        newsnapshot.setStatus(Status.DOWNLOADED);
        newsnapshot.setType(MockVolumeType.SNAPSHOT);
        newsnapshot = _mockVolumeDao.persist(newsnapshot);
        
        return new BackupSnapshotAnswer(cmd, true, null, newsnapshot.getName(), true);
    }

    @Override
    public Answer DeleteSnapshotBackup(DeleteSnapshotBackupCommand cmd) {
        
        MockVolumeVO backSnapshot = _mockVolumeDao.findByName(cmd.getSnapshotUuid());
        if (backSnapshot == null) {
            return new Answer(cmd, false, "can't find the backupsnapshot: " + cmd.getSnapshotUuid());
        }
        
        _mockVolumeDao.remove(backSnapshot.getId());
        
        return new Answer(cmd);
    }

    @Override
    public CreateVolumeFromSnapshotAnswer CreateVolumeFromSnapshot(CreateVolumeFromSnapshotCommand cmd) {
        MockVolumeVO backSnapshot = _mockVolumeDao.findByName(cmd.getSnapshotUuid());
        if (backSnapshot == null) {
            return new CreateVolumeFromSnapshotAnswer(cmd, false, "can't find the backupsnapshot: " + cmd.getSnapshotUuid(), null);
        }
        
        MockStoragePoolVO primary = _mockStoragePoolDao.findByUuid(cmd.getPrimaryStoragePoolNameLabel());
        if (primary == null) {
            return new CreateVolumeFromSnapshotAnswer(cmd, false, "can't find the primary storage: " + cmd.getPrimaryStoragePoolNameLabel(), null);
        }
        
        String uuid = UUID.randomUUID().toString();
        MockVolumeVO volume = new MockVolumeVO();
        
        volume.setName(uuid);
        volume.setPath(primary.getMountPoint() + uuid);
        volume.setPoolId(primary.getId());
        volume.setSize(backSnapshot.getSize());
        volume.setStatus(Status.DOWNLOADED);
        volume.setType(MockVolumeType.VOLUME);
        _mockVolumeDao.persist(volume);
        
        return new CreateVolumeFromSnapshotAnswer(cmd, true, null, volume.getPath());
    }

    @Override
    public Answer DeleteTemplate(DeleteTemplateCommand cmd) {
        MockVolumeVO template = _mockVolumeDao.findByStoragePathAndType(cmd.getTemplatePath());
        if (template == null) {
            return new Answer(cmd, false, "can't find template:" + cmd.getTemplatePath());
        }
        
        _mockVolumeDao.remove(template.getId());
        
        return new Answer(cmd);
    }

    @Override
    public Answer SecStorageVMSetup(SecStorageVMSetupCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
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

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void preinstallTemplates(String url, long zoneId) {
        MockSecStorageVO storage = _mockSecStorageDao.findByUrl(url);
        if (storage == null) {
            storage = new MockSecStorageVO();
            URI uri;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                return;
            }
            
            String nfsHost = uri.getHost();
            String nfsPath = uri.getPath();
            String path = nfsHost + ":" + nfsPath;
            String dir = "/mnt/" + UUID.nameUUIDFromBytes(path.getBytes()).toString() + File.separator;
            
            storage.setUrl(url);
            storage.setCapacity(DEFAULT_HOST_STORAGE_SIZE);
           
            storage.setMountPoint(dir);
            
            storage = _mockSecStorageDao.persist(storage);
            
            //preinstall default templates into secondary storage
            long defaultTemplateSize = 2 * 1024 * 1024 * 1024L;
            MockVolumeVO template = new MockVolumeVO();
            template.setName("simulator-domR");
            template.setPath(storage.getMountPoint() + "template/tmpl/1/9/" + UUID.randomUUID().toString());
            template.setPoolId(storage.getId());
            template.setSize(defaultTemplateSize);
            template.setType(MockVolumeType.TEMPLATE);
            template.setStatus(Status.DOWNLOADED);
            _mockVolumeDao.persist(template);
            
            template = new MockVolumeVO();
            template.setName("simulator-Centos");
            template.setPath(storage.getMountPoint() + "template/tmpl/1/10/" + UUID.randomUUID().toString());
            template.setPoolId(storage.getId());
            template.setSize(defaultTemplateSize);
            template.setType(MockVolumeType.TEMPLATE);
            _mockVolumeDao.persist(template);
        }
        
    }
    
    @Override
    public StoragePoolInfo getLocalStorage(String hostGuid) {
        MockHost host = _mockHostDao.findByGuid(hostGuid);
        
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findByHost(hostGuid);
        if (storagePool == null) {
            String uuid = UUID.randomUUID().toString();
            storagePool = new MockStoragePoolVO();
            storagePool.setUuid(uuid);
            storagePool.setMountPoint("/mnt/" + uuid + File.separator);
            storagePool.setCapacity(DEFAULT_HOST_STORAGE_SIZE);
            storagePool.setHostGuid(hostGuid);
            storagePool.setStorageType(StoragePoolType.Filesystem);
            storagePool =  _mockStoragePoolDao.persist(storagePool);
        }
        
        
        return new StoragePoolInfo(storagePool.getUuid(), host.getPrivateIpAddress(), storagePool.getMountPoint(), storagePool.getMountPoint(), storagePool.getPoolType(), storagePool.getCapacity(), 0 );
    }
    
    @Override
    public StoragePoolInfo getLocalStorage(String hostGuid, Long storageSize) {
        MockHost host = _mockHostDao.findByGuid(hostGuid);
        if (storageSize == null) {
            storageSize = DEFAULT_HOST_STORAGE_SIZE;
        }
        MockStoragePoolVO storagePool = _mockStoragePoolDao.findByHost(hostGuid);
        if (storagePool == null) {
            String uuid = UUID.randomUUID().toString();
            storagePool = new MockStoragePoolVO();
            storagePool.setUuid(uuid);
            storagePool.setMountPoint("/mnt/" + uuid + File.separator);
            storagePool.setCapacity(storageSize);
            storagePool.setHostGuid(hostGuid);
            storagePool.setStorageType(StoragePoolType.Filesystem);
            storagePool =  _mockStoragePoolDao.persist(storagePool);
        }
        
        
        return new StoragePoolInfo(storagePool.getUuid(), host.getPrivateIpAddress(), storagePool.getMountPoint(), storagePool.getMountPoint(), storagePool.getPoolType(), storagePool.getCapacity(), 0 );
    }
    
    @Override
    public CreatePrivateTemplateAnswer CreatePrivateTemplateFromSnapshot(CreatePrivateTemplateFromSnapshotCommand cmd) {
        String snapshotUUId = cmd.getSnapshotUuid();
        MockVolumeVO snapshot = _mockVolumeDao.findByName(snapshotUUId);
        if (snapshot == null) {
            snapshotUUId = cmd.getSnapshotName();
            snapshot = _mockVolumeDao.findByName(snapshotUUId);
            if (snapshot == null) {
                return new CreatePrivateTemplateAnswer(cmd, false, "can't find snapshot:" + snapshotUUId);
            }
        }
        
        MockSecStorageVO sec = _mockSecStorageDao.findByUrl(cmd.getSecondaryStoragePoolURL());
        if (sec == null) {
            return new CreatePrivateTemplateAnswer(cmd, false, "can't find secondary storage");
        }
        
        MockVolumeVO template = new MockVolumeVO();
        String uuid = UUID.randomUUID().toString();
        template.setName(uuid);
        template.setPath(sec.getMountPoint() + uuid);
        template.setPoolId(sec.getId());
        template.setSize(snapshot.getSize());
        template.setStatus(Status.DOWNLOADED);
        template.setType(MockVolumeType.TEMPLATE);
        template = _mockVolumeDao.persist(template);
        
        return new CreatePrivateTemplateAnswer(cmd, true, "", template.getName(), template.getSize(), template.getSize(), template.getName(), ImageFormat.QCOW2);
    }
    
    @Override
    public Answer ComputeChecksum(ComputeChecksumCommand cmd) {
        MockVolumeVO volume = _mockVolumeDao.findByName(cmd.getTemplatePath());
        if (volume == null) {
            return new Answer(cmd, false, "cant' find volume:" + cmd.getTemplatePath());
        }
        String md5 = null;
        try {
            MessageDigest md =  MessageDigest.getInstance("md5");
            md5 = String.format("%032x", new BigInteger(1, md.digest(cmd.getTemplatePath().getBytes())));
        } catch (NoSuchAlgorithmException e) {
            s_logger.debug("failed to gernerate md5:" + e.toString());
        }
        
        return new Answer(cmd, true, md5);
    }
    @Override
    public CreatePrivateTemplateAnswer CreatePrivateTemplateFromVolume(CreatePrivateTemplateFromVolumeCommand cmd) {
        MockVolumeVO volume = _mockVolumeDao.findByStoragePathAndType(cmd.getVolumePath());
        if (volume == null) {
            return new CreatePrivateTemplateAnswer(cmd, false, "cant' find volume" + cmd.getVolumePath());
        }
        
        MockSecStorageVO sec = _mockSecStorageDao.findByUrl(cmd.getSecondaryStoragePoolURL());
        if (sec == null) {
            return new CreatePrivateTemplateAnswer(cmd, false, "can't find secondary storage");
        }
        
        MockVolumeVO template = new MockVolumeVO();
        String uuid = UUID.randomUUID().toString();
        template.setName(uuid);
        template.setPath(sec.getMountPoint() + uuid);
        template.setPoolId(sec.getId());
        template.setSize(volume.getSize());
        template.setStatus(Status.DOWNLOADED);
        template.setType(MockVolumeType.TEMPLATE);
        template = _mockVolumeDao.persist(template);
        
        return new CreatePrivateTemplateAnswer(cmd, true, "", template.getName(), template.getSize(), template.getSize(), template.getName(), ImageFormat.QCOW2);
    }
    
}

