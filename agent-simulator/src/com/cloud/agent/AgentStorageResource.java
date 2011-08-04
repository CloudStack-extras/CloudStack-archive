/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
 *
 * This software is licensed under the GNU General Public License v3 or later.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cloud.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.BackupSnapshotAnswer;
import com.cloud.agent.api.BackupSnapshotCommand;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckStateAnswer;
import com.cloud.agent.api.CheckStateCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreateVolumeFromSnapshotAnswer;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.DeleteSnapshotBackupAnswer;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.GetFileStatsAnswer;
import com.cloud.agent.api.GetFileStatsCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.ManageSnapshotAnswer;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingStorageCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.SecStorageSetupAnswer;
import com.cloud.agent.api.SecStorageSetupCommand;
import com.cloud.agent.api.SecStorageVMSetupCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupSecondaryStorageCommand;
import com.cloud.agent.api.storage.CreatePrivateTemplateAnswer;
import com.cloud.agent.api.storage.CreatePrivateTemplateCommand;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.storage.UpgradeDiskAnswer;
import com.cloud.agent.api.storage.UpgradeDiskCommand;
import com.cloud.agent.api.storage.ssCommand;
import com.cloud.agent.manager.SimulatorManager;
import com.cloud.agent.manager.SimulatorManager.AgentType;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.storage.resource.SecondaryStorageResource;
import com.cloud.storage.template.DownloadManager;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.VirtualMachine.State;

public class AgentStorageResource extends AgentResourceBase implements SecondaryStorageResource {
    private static final Logger s_logger = Logger.getLogger(AgentStorageResource.class);

    final protected String _parent = "/mnt/SecStorage";
    protected String _role;
    private transient DownloadManager _dlMgr;

	private String _publicIp;

    public AgentStorageResource(long instanceId, AgentType agentType, SimulatorManager simMgr) {
        super(instanceId, agentType, simMgr);
    }

    public AgentStorageResource() {
        setType(Host.Type.SecondaryStorage);
    }

    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof GetFileStatsCommand) {
            return execute((GetFileStatsCommand) cmd);
        } else if (cmd instanceof GetStorageStatsCommand) {
            return execute((GetStorageStatsCommand) cmd);
        } else if (cmd instanceof DownloadProgressCommand) {
        	return _dlMgr.handleDownloadCommand(this, (DownloadProgressCommand)cmd);
        } else if (cmd instanceof DownloadCommand) {
        	return _dlMgr.handleDownloadCommand(this, (DownloadCommand)cmd);
        } else if (cmd instanceof CheckStateCommand) {
            return execute((CheckStateCommand) cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return execute((CheckHealthCommand) cmd);
        } else if (cmd instanceof UpgradeDiskCommand) {
            return execute((UpgradeDiskCommand) cmd);
        } else if (cmd instanceof CreatePrivateTemplateCommand) {
            return execute((CreatePrivateTemplateCommand) cmd);
        } else if (cmd instanceof DeleteTemplateCommand) {
			return execute((DeleteTemplateCommand) cmd);
		} else if (cmd instanceof ReadyCommand) {
			return execute((ReadyCommand) cmd);
		} else if (cmd instanceof SecStorageSetupCommand) {
			return execute((SecStorageSetupCommand) cmd);
		} else if (cmd instanceof SecStorageVMSetupCommand) {
			return execute((SecStorageVMSetupCommand) cmd);
		} else if (cmd instanceof ListTemplateCommand) {
			return execute((ListTemplateCommand) cmd);
		} else {
			return Answer.createUnsupportedCommandAnswer(cmd);
		}
	}

    private Answer execute(ListTemplateCommand cmd) {
        Map<String, TemplateInfo> tinfo = new HashMap<String, TemplateInfo>();
        populateTemplateStartupInfo(tinfo);
		return new ListTemplateAnswer(cmd.getSecUrl(), tinfo);
	}

	private Answer execute(SecStorageVMSetupCommand cmd) {
		return new Answer(cmd, true, null);
	}

	private Answer execute(SecStorageSetupCommand cmd) {
		String secUrl = cmd.getSecUrl();
		try {
			URI uri = new URI(secUrl);
			String nfsHost = uri.getHost();

			InetAddress nfsHostAddr = InetAddress.getByName(nfsHost);
			String nfsHostIp = nfsHostAddr.getHostAddress();
			String nfsPath = nfsHostIp + ":" + uri.getPath();
			String dir = UUID.nameUUIDFromBytes(nfsPath.getBytes()).toString();
			return new SecStorageSetupAnswer(dir);
		} catch (Exception e) {
			String msg = "GetRootDir for " + secUrl + " failed due to "
					+ e.toString();
			s_logger.error(msg);
			return new Answer(cmd, false, msg);
		}
	}

    
    @Override
    public PingCommand getCurrentStatus(long id) {
        return new PingStorageCommand(Host.Type.Storage, id, new HashMap<String, Boolean>());
    }

    @Override
    public Type getType() {
    	if(SecondaryStorageVm.Role.templateProcessor.toString().equals(_role))
    		return Host.Type.SecondaryStorage;    	
    	return Host.Type.SecondaryStorageCmdExecutor;
    }
        
    

    @Override
    public StartupCommand[] initialize() {
        Map<String, TemplateInfo> tInfo = new HashMap<String, TemplateInfo>();
        populateTemplateStartupInfo(tInfo);
        StartupSecondaryStorageCommand cmd = new StartupSecondaryStorageCommand();

        cmd.setPrivateIpAddress(getHostPrivateIp());
        cmd.setPrivateNetmask("255.255.0.0");
        cmd.setPrivateMacAddress(getHostMacAddress().toString());
        cmd.setStorageIpAddress(getHostStoragePrivateIp());
        cmd.setStorageNetmask("255.255.0.0");
        cmd.setStorageMacAddress(getHostStorageMacAddress().toString());
        cmd.setStorageIpAddressDeux(getHostStoragePrivateIp2());
        cmd.setStorageNetmaskDeux("255.255.0.0");
        cmd.setStorageMacAddressDeux(getHostStorageMacAddress2().toString());
        if(_publicIp != null)
            cmd.setPublicIpAddress(_publicIp);

        cmd.setName(getName());
        cmd.setAgentTag("agent-simulator");
        cmd.setVersion(getVersion());
        cmd.setDataCenter(Long.toString(getZone()));
        cmd.setIqn(getHostIqn());
        cmd.setPod(getPod());
        cmd.setCluster(getCluster());
        
        getSimulatorManager().saveResourceState(null, this);
        return new StartupCommand[] { cmd };
    }

    protected Answer execute(DestroyCommand cmd) {
        return new Answer(cmd, true, null);
    }

    protected Answer execute(GetFileStatsCommand cmd) {
        String image = cmd.getPaths();
        return new GetFileStatsAnswer(cmd, getStorageMgr().getSize(image));
    }

  
    protected Answer execute(DownloadCommand cmd) {
        DownloadAnswer answer;
        if (cmd instanceof DownloadProgressCommand) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("Executing download-progress command, url: " + cmd.getUrl() + ", name: " + cmd.getName() + ", description: " + cmd.getDescription() + ", job: "
                        + ((DownloadProgressCommand) cmd).getJobId());
            }

            String jobId = ((DownloadProgressCommand) cmd).getJobId();
            if (jobId == null) {
                jobId = String.valueOf(System.currentTimeMillis());
            }

            switch (((DownloadProgressCommand) cmd).getRequest()) {
            case GET_STATUS:
                break;
            case ABORT:
                break;
            case RESTART:
                break;
            case PURGE:
                answer = new DownloadAnswer(jobId, 100, cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED, "/tmp/" + jobId, "/template/" + jobId);
                answer.setTemplateSize(200 * 1024 * 1024L);
                return answer;

            default:
                break;
            }
        }

        String jobId = ((DownloadProgressCommand) cmd).getJobId();
        if (jobId == null) {
            jobId = String.valueOf(System.currentTimeMillis());
        }

        answer = new DownloadAnswer(jobId, 100, cmd, com.cloud.storage.VMTemplateStorageResourceAssoc.Status.DOWNLOADED, "/tmp/" + jobId, "/template/" + jobId);
        answer.setTemplateSize(200 * 1024 * 1024L);
        return answer;
    }

    protected GetVncPortAnswer execute(GetVncPortCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Executing getVncPort command, vm: " + cmd.getId() + ", name: " + cmd.getName());
        }

        VmMgr vmMgr = getVmMgr();
        int port = vmMgr.getVncPort(cmd.getName());

        // make it more real by plus 5900 base port number
        if (port >= 0) {
            port += 5900;
        }

        if (s_logger.isInfoEnabled()) {
            s_logger.info("Return vnc port: " + port + " for " + cmd.getName());
        }

        return new GetVncPortAnswer(cmd, port);
    }

	protected GetStorageStatsAnswer execute(GetStorageStatsCommand cmd) {
		//TODO: This is not the same as primary storage stats given out by AgentRoutingResource
		//Should be based on templates and snapshots on the storage
		long size = getStorageMgr().getUsedSize();
		return size != -1 ? new GetStorageStatsAnswer(cmd, getStorageMgr()
				.getTotalSize(), size) : new GetStorageStatsAnswer(cmd,
				"Unable to get storage stats");
	}

    protected CheckStateAnswer execute(CheckStateCommand cmd) {
        return new CheckStateAnswer(cmd, State.Unknown, "Not Implemented");
    }

    protected CheckHealthAnswer execute(CheckHealthCommand cmd) {
        return new CheckHealthAnswer(cmd, true);
    }

    protected UpgradeDiskAnswer execute(UpgradeDiskCommand cmd) {
        return new UpgradeDiskAnswer(cmd, true, null);
    }

    protected ManageSnapshotAnswer execute(final ManageSnapshotCommand cmd) {
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Manage snapshot command {" + cmd.getCommandSwitch() + ", " + cmd.getVolumePath() + ", " + cmd.getSnapshotId() + "}");
        }

        return new ManageSnapshotAnswer(cmd, true, null);
    }

    protected CreatePrivateTemplateAnswer execute(final CreatePrivateTemplateCommand cmd) {
        CreatePrivateTemplateAnswer answer = new CreatePrivateTemplateAnswer(cmd, true, null);
        answer.setPath("");
        return answer;
    }

    protected BackupSnapshotAnswer execute(final BackupSnapshotCommand cmd) {
        return new BackupSnapshotAnswer(cmd, true, null, UUID.randomUUID().toString(), true);
    }

    protected DeleteSnapshotBackupAnswer execute(final DeleteSnapshotBackupCommand cmd) {
        return new DeleteSnapshotBackupAnswer(cmd, true, "1");
    }

    protected CreateVolumeFromSnapshotAnswer execute(final CreateVolumeFromSnapshotCommand cmd) {
        return new CreateVolumeFromSnapshotAnswer(cmd, true, null, UUID.randomUUID().toString());
    }

    protected Answer execute(final DeleteTemplateCommand cmd) {
        return new Answer(cmd, true, null);
    }

    protected Answer execute(final ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }

    protected String getUserPath(String image) {
        return image.substring(0, image.indexOf("/", _parent.length() + 2)).intern();
    }

    protected boolean isMounted(String[] paths) {
        return false;
    }

    public static void populateTemplateStartupInfo(Map<String, TemplateInfo> info) {
        info.put("routing", new TemplateInfo("routing","tank/volumes/demo/template/private/u000000/os/routing", false, false));
        info.put("consoleproxy", new TemplateInfo("consoleproxy", "tank/volumes/demo/template/private/u000000/os/consoleproxy", false, false));

        info.put("centos53-x86_64", new TemplateInfo("centos53-x86_64", "tank/volumes/demo/template/public/os/centos53-x86_64", true, false));
        info.put("win2003sp2", new TemplateInfo("win2003sp2", "tank/volumes/demo/template/public/os/win2003sp2", true, false));
        info.put("winxpsp3", new TemplateInfo("winxpsp3", "tank/volumes/demo/template/public/os/winxpsp3", true, false));
        info.put("fedora10-x86_64", new TemplateInfo("fedora10-x86_64", "tank/volumes/demo/template/public/os/fedora10-x86_64", true, false));
        info.put("fedora9-x86_64", new TemplateInfo("fedora9-x86_64", "tank/volumes/demo/template/public/os/fedora9-x86_64", true, false));
        info.put("centos52-x86_64", new TemplateInfo("centos52-x86_64", "tank/volumes/demo/template/public/os/centos52-x86_64", true, false));
    }

    private void populateTemplateStorage() {
        MockStorageMgr storageMgr = getStorageMgr();
        long size = 200 * 1024 * 1024L;

        String volumePath = "tank/volumes/demo/template/private/u000000/os/routing";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/private/u000000/os/consoleproxy";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/centos53-x86_64";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/win2003sp2";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/winxpsp3";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/fedora10-x86_64";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/fedora9-x86_64";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));

        volumePath = "tank/volumes/demo/template/public/os/centos52-x86_64";
        storageMgr.addVolume(volumePath, new MockVolume(volumePath, size));
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if (!super.configure(name, params)) {
            s_logger.warn("Base class was unable to configure");
            return false;
        }

        String value = (String) params.get("instance");
        if (value == null) {
            value = (String) params.get("parent");
            if (value == null) {
                value = "vmops";
            }
        }
        if (!value.endsWith("/")) {
            value = value + "/";
        }
        s_logger.info("Storage parent path : " + _parent  + File.pathSeparator + getGuid());
        
        String eth2ip = (String) params.get("eth2ip");
        if (eth2ip != null) {
            params.put("public.network.device", "eth2");
        }         
        _publicIp = (String) params.get("eth2ip");

        populateTemplateStorage();
        return true;
    }

	@Override
	public String getRootDir(ssCommand cmd) {
		try {
			URI uri = new URI(cmd.getSecUrl());
			String nfsHost = uri.getHost();

			InetAddress nfsHostAddr = InetAddress.getByName(nfsHost);
			String nfsHostIp = nfsHostAddr.getHostAddress();
			String nfsPath = nfsHostIp + ":" + uri.getPath();
			String dir = UUID.nameUUIDFromBytes(nfsPath.getBytes()).toString();
			String root = _parent + File.pathSeparator + getGuid();
			return root;
		} catch (Exception e) {
			String msg = "GetRootDir for " + cmd.getSecUrl() + " failed due to "
					+ e.toString();
			s_logger.error(msg, e);
			throw new CloudRuntimeException(msg);
		}
	}
}
