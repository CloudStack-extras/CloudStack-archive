package com.cloud.storage.resource;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingStorageCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.SecStorageSetupCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResource;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.Volume;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.template.DownloadManager;
import com.cloud.storage.template.DownloadManagerImpl;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.Script;

public class GlusterSecondaryStorageResource extends ServerResourceBase
implements ServerResource {
	private static final Logger s_logger = Logger.getLogger(NfsSecondaryStorageResource.class);
	int _timeout;
	DownloadManager _dlMgr;
	StorageLayer _storage;
	String _parent;
	String _dc;
	String _pod;
	String _guid;
	String _glusterPath;
	String _mountParent;
	Map<String, Object> _params;
	Random _rand = new Random(System.currentTimeMillis());
	
	@Override
	public Type getType() {
		return Host.Type.SecondaryStorage;
	}

	@Override
	public StartupCommand[] initialize() {
		final StartupStorageCommand cmd = new StartupStorageCommand(_parent, StoragePoolType.GlusterFS, getTotalSize(), new HashMap<String, TemplateInfo>());

		cmd.setResourceType(Volume.StorageResourceType.SECONDARY_STORAGE);
		cmd.setIqn(null);

		fillNetworkInformation(cmd);
		cmd.setDataCenter(_dc);
		cmd.setPod(_pod);
		cmd.setGuid(_guid);
		cmd.setName(_guid);
		cmd.setVersion(GlusterSecondaryStorageResource.class.getPackage().getImplementationVersion());
		/* gather TemplateInfo in second storage */
		final Map<String, TemplateInfo> tInfo = _dlMgr.gatherTemplateInfo();
		cmd.setTemplateInfo(tInfo);
		cmd.getHostDetails().put("mount.parent", _mountParent);
		cmd.getHostDetails().put("mount.path", _glusterPath);
		String tok[] = _glusterPath.split(":");
		cmd.setNfsShare("gluster://" + tok[0] + tok[1]);
		if (cmd.getHostDetails().get("orig.url") == null) {
			if (tok.length != 2) {
				throw new CloudRuntimeException("Not valid Gluster path" + _glusterPath);
			}
			String glusterUrl = "gluster://" + tok[0] + tok[1];
			cmd.getHostDetails().put("orig.url", glusterUrl);
		}
		InetAddress addr;
		try {
			addr = InetAddress.getByName(tok[0]);
			cmd.setPrivateIpAddress(addr.getHostAddress());
		} catch (UnknownHostException e) {
			cmd.setPrivateIpAddress(tok[0]);
		}
		return new StartupCommand [] {cmd};
	}

	@Override
	public PingCommand getCurrentStatus(long id) {
		 return new PingStorageCommand(Host.Type.Storage, id, new HashMap<String, Boolean>());
	}

	@Override
	public Answer executeRequest(Command cmd) {
		if (cmd instanceof DownloadProgressCommand) {
			return _dlMgr.handleDownloadCommand((DownloadProgressCommand)cmd);
		} else if (cmd instanceof DownloadCommand) {
			return _dlMgr.handleDownloadCommand((DownloadCommand)cmd);
		} else if (cmd instanceof GetStorageStatsCommand) {
			return execute((GetStorageStatsCommand)cmd);
		} else if (cmd instanceof CheckHealthCommand) {
			return new CheckHealthAnswer((CheckHealthCommand)cmd, true);
		} else if (cmd instanceof DeleteTemplateCommand) {
			return execute((DeleteTemplateCommand) cmd);
		} else if (cmd instanceof ReadyCommand) {
			return new ReadyAnswer((ReadyCommand)cmd);
		} else if (cmd instanceof SecStorageSetupCommand){
			return execute((SecStorageSetupCommand)cmd);
		} else {
			return Answer.createUnsupportedCommandAnswer(cmd);
		}
	}

	private Answer execute(SecStorageSetupCommand cmd) {
		return new Answer(cmd, true, null);
	}

	protected long getUsedSize() {
		return _storage.getUsedSpace(_parent);
	}

	protected long getTotalSize() {
		return _storage.getTotalSpace(_parent);
	}

	protected GetStorageStatsAnswer execute(final GetStorageStatsCommand cmd) {
		final long usedSize = getUsedSize();
		final long totalSize = getTotalSize();
		if (usedSize == -1 || totalSize == -1) {
			return new GetStorageStatsAnswer(cmd, "Unable to get storage stats");
		} else {
			return new GetStorageStatsAnswer(cmd, totalSize, usedSize) ;
		}
	}

	protected Answer execute(final DeleteTemplateCommand cmd) {
		String relativeTemplatePath = cmd.getTemplatePath();
		String parent = _parent;

		if (relativeTemplatePath.startsWith(File.separator)) {
			relativeTemplatePath = relativeTemplatePath.substring(1);
		}

		if (!parent.endsWith(File.separator)) {
			parent += File.separator;
		}
		String absoluteTemplatePath = parent + relativeTemplatePath;
		File tmpltParent = new File(absoluteTemplatePath).getParentFile();

		boolean result = true;
		if (tmpltParent.exists()) {
			File [] tmpltFiles = tmpltParent.listFiles();
			if (tmpltFiles != null) {
				for (File f : tmpltFiles) {
					f.delete();
				}
			}

			result = _storage.delete(tmpltParent.getAbsolutePath());
		}

		if (result) {
			return new Answer(cmd, true, null);
		} else {
			return new Answer(cmd, false, "Failed to delete file");
		}
	}

	@Override
	protected String getDefaultScriptsDir() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		super.configure(name, params);
		String value = (String)params.get("scripts.timeout");
		_timeout = NumbersUtil.parseInt(value, 1440) * 1000;

		_storage = (StorageLayer)params.get(StorageLayer.InstanceConfigKey);
		if (_storage == null) {
			value = (String)params.get(StorageLayer.ClassConfigKey);
			if (value == null) {
				value = "com.cloud.storage.JavaStorageLayer";
			}

			try {
				Class<?> clazz = Class.forName(value);
				_storage = (StorageLayer)ComponentLocator.inject(clazz);
				_storage.configure("StorageLayer", params);
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("Unable to find class " + value);
			}
		}

		_guid = (String)params.get("guid");
		if (_guid == null) {
			throw new ConfigurationException("Unable to find the guid");
		}

		_dc = (String)params.get("zone");
		if (_dc == null) {
			throw new ConfigurationException("Unable to find the zone");
		}
		_pod = (String)params.get("pod");



		_mountParent = (String)params.get("mount.parent");
		if (_mountParent == null) {
			_mountParent = File.separator + "mnt";
		}

		_glusterPath = (String)params.get("mount.path");
		if (_glusterPath == null) {
			throw new ConfigurationException("Unable to find mount.path");
		}

		_parent = mount(_glusterPath, _mountParent);
		if (_parent == null) {
			throw new ConfigurationException("Unable to create mount point");
		}


		s_logger.info("Mount point established at " + _parent);
		_params = params;
		try {
			_params.put("template.parent", _parent);
			_params.put(StorageLayer.InstanceConfigKey, _storage);
			_dlMgr = new DownloadManagerImpl();
			_dlMgr.configure("DownloadManager", _params);
		} catch (ConfigurationException e) {
			s_logger.warn("Caught problem while configuring DownloadManager", e);
			return false;
		}
		return true;
	}

	protected String mount(String path, String parent) {
		String mountPoint = null;
		for (int i = 0; i < 10; i++) {
			String mntPt = parent + File.separator + Integer.toHexString(_rand.nextInt(Integer.MAX_VALUE));
			File file = new File(mntPt);
			if (!file.exists()) {
				if (_storage.mkdir(mntPt)) {
					mountPoint = mntPt;
					break;
				}
			}
			s_logger.debug("Unable to create mount: " + mntPt);
		}

		if (mountPoint == null) {
			s_logger.warn("Unable to create a mount point");
			return null;
		}

		Script script = null;
		String result = null;
		script = new Script(true, "umount", _timeout, s_logger);
		script.add(path);
		result = script.execute();

		if( _parent != null ) {
			script = new Script("rmdir", _timeout, s_logger);
			script.add(_parent);
			result = script.execute();
		}

		Script command = new Script(true, "mount", _timeout, s_logger);
		command.add("-t", "gluster");	    
		command.add(path);
		command.add(mountPoint);
		result = command.execute();
		if (result != null) {
			s_logger.warn("Unable to mount " + path + " due to " + result);
			File file = new File(mountPoint);
			if (file.exists())
				file.delete();
			return null;
		}

		// Change permissions for the mountpoint
		script = new Script(true, "chmod", _timeout, s_logger);
		script.add("777", mountPoint);
		result = script.execute();
		if (result != null) {
			s_logger.warn("Unable to set permissions for " + mountPoint + " due to " + result);
			return null;
		}

		// XXX: Adding the check for creation of snapshots dir here. Might have to move it somewhere more logical later.
		if (!checkForSnapshotsDir(mountPoint)) {
			return null;
		}

		// Create the volumes dir
		if (!checkForVolumesDir(mountPoint)) {
			return null;
		}

		return mountPoint;
	}

	protected boolean checkForSnapshotsDir(String mountPoint) {
		String snapshotsDirLocation = mountPoint + File.separator + "snapshots";
		return createDir("snapshots", snapshotsDirLocation, mountPoint);
	}

	protected boolean checkForVolumesDir(String mountPoint) {
		String volumesDirLocation = mountPoint + "/" + "volumes";
		return createDir("volumes", volumesDirLocation, mountPoint);
	}

	protected boolean createDir(String dirName, String dirLocation, String mountPoint) {
		boolean dirExists = false;

		File dir = new File(dirLocation);
		if (dir.exists()) {
			if (dir.isDirectory()) {
				s_logger.debug(dirName + " already exists on secondary storage, and is mounted at " + mountPoint);
				dirExists = true;
			} else {
				if (dir.delete() && _storage.mkdir(dirLocation)) {
					dirExists = true;
				}
			}
		} else if (_storage.mkdir(dirLocation)) {
			dirExists = true;
		}

		if (dirExists) {
			s_logger.info(dirName  + " directory created/exists on Secondary Storage.");
		} else {
			s_logger.info(dirName + " directory does not exist on Secondary Storage.");
		}

		return dirExists;
	}
}
