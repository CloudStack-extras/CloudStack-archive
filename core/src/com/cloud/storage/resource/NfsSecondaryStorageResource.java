/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.storage.resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CleanupSnapshotBackupCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ComputeChecksumCommand;
import com.cloud.agent.api.DeleteObjectFromSwiftCommand;
import com.cloud.agent.api.DeleteSnapshotBackupCommand;
import com.cloud.agent.api.DeleteSnapshotsDirCommand;
import com.cloud.agent.api.GetStorageStatsAnswer;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.PingCommand;
import com.cloud.agent.api.PingStorageCommand;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand.PortConfig;
import com.cloud.agent.api.SecStorageSetupAnswer;
import com.cloud.agent.api.SecStorageSetupCommand;
import com.cloud.agent.api.SecStorageSetupCommand.Certificates;
import com.cloud.agent.api.StartupSecondaryStorageCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand.PortConfig;
import com.cloud.agent.api.SecStorageVMSetupCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupSecondaryStorageCommand;
import com.cloud.agent.api.downloadSnapshotFromSwiftCommand;
import com.cloud.agent.api.downloadTemplateFromSwiftToSecondaryStorageCommand;
import com.cloud.agent.api.uploadTemplateToSwiftFromSecondaryStorageCommand;
import com.cloud.agent.api.storage.CreateEntityDownloadURLCommand;
import com.cloud.agent.api.storage.DeleteEntityDownloadURLCommand;
import com.cloud.agent.api.storage.DeleteTemplateCommand;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.agent.api.storage.DownloadProgressCommand;
import com.cloud.agent.api.storage.ListTemplateAnswer;
import com.cloud.agent.api.storage.ListTemplateCommand;
import com.cloud.agent.api.storage.UploadCommand;
import com.cloud.agent.api.storage.ssCommand;
import com.cloud.agent.api.to.SwiftTO;
import com.cloud.exception.InternalErrorException;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.resource.ServerResourceBase;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.template.DownloadManager;
import com.cloud.storage.template.DownloadManagerImpl;
import com.cloud.storage.template.DownloadManagerImpl.ZfsPathParser;
import com.cloud.storage.template.TemplateInfo;
import com.cloud.storage.template.TemplateLocation;
import com.cloud.storage.template.UploadManager;
import com.cloud.storage.template.UploadManagerImpl;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;
import com.cloud.vm.SecondaryStorageVm;

public class NfsSecondaryStorageResource extends ServerResourceBase implements SecondaryStorageResource {
    private static final Logger s_logger = Logger.getLogger(NfsSecondaryStorageResource.class);
    int _timeout;
    
    String _instance;  
    String _dc;
    String _pod;
    String _guid;
    String _role;
    Map<String, Object> _params;
    StorageLayer _storage;
    boolean _inSystemVM = false;
    boolean _sslCopy = false;
    
    DownloadManager _dlMgr;
    UploadManager _upldMgr;
	private String _configSslScr;
	private String _configAuthScr;
	private String _configIpFirewallScr;
	private String _publicIp;
	private String _hostname;
	private String _localgw;
	private String _eth1mask;
	private String _eth1ip;
	private String _storageIp;
	private String _storageNetmask;
	private String _storageGateway;
	final private String _parent = "/mnt/SecStorage";
	final private String _tmpltDir = "/var/cloudstack/template";
    final private String _tmpltpp = "template.properties";
    @Override
    public void disconnected() {
    }

    @Override
    public Answer executeRequest(Command cmd) {
        if (cmd instanceof DownloadProgressCommand) {
            return _dlMgr.handleDownloadCommand(this, (DownloadProgressCommand)cmd);
        } else if (cmd instanceof DownloadCommand) {
            return _dlMgr.handleDownloadCommand(this, (DownloadCommand)cmd);
        } else if (cmd instanceof UploadCommand) {        	
            return _upldMgr.handleUploadCommand(this, (UploadCommand)cmd);
        } else if (cmd instanceof CreateEntityDownloadURLCommand){
            return _upldMgr.handleCreateEntityURLCommand((CreateEntityDownloadURLCommand)cmd);
        } else if(cmd instanceof DeleteEntityDownloadURLCommand){
            return _upldMgr.handleDeleteEntityDownloadURLCommand((DeleteEntityDownloadURLCommand)cmd);
        } else if (cmd instanceof GetStorageStatsCommand) {
        	return execute((GetStorageStatsCommand)cmd);
        } else if (cmd instanceof CheckHealthCommand) {
            return new CheckHealthAnswer((CheckHealthCommand)cmd, true);
        } else if (cmd instanceof DeleteTemplateCommand) {
        	return execute((DeleteTemplateCommand) cmd);
        } else if (cmd instanceof ReadyCommand) {
            return new ReadyAnswer((ReadyCommand)cmd);
        } else if (cmd instanceof SecStorageFirewallCfgCommand){
        	return execute((SecStorageFirewallCfgCommand)cmd);
        } else if (cmd instanceof SecStorageVMSetupCommand){
        	return execute((SecStorageVMSetupCommand)cmd);
        } else if (cmd instanceof SecStorageSetupCommand){
            return execute((SecStorageSetupCommand)cmd);
        } else if (cmd instanceof ComputeChecksumCommand){
            return execute((ComputeChecksumCommand)cmd);
        } else if (cmd instanceof ListTemplateCommand){
            return execute((ListTemplateCommand)cmd);
        } else if (cmd instanceof downloadSnapshotFromSwiftCommand){
            return execute((downloadSnapshotFromSwiftCommand)cmd);
        } else if (cmd instanceof DeleteSnapshotBackupCommand){
            return execute((DeleteSnapshotBackupCommand)cmd);
        } else if (cmd instanceof DeleteSnapshotsDirCommand){
            return execute((DeleteSnapshotsDirCommand)cmd);
        } else if (cmd instanceof downloadTemplateFromSwiftToSecondaryStorageCommand) {
            return execute((downloadTemplateFromSwiftToSecondaryStorageCommand) cmd);
        } else if (cmd instanceof uploadTemplateToSwiftFromSecondaryStorageCommand) {
            return execute((uploadTemplateToSwiftFromSecondaryStorageCommand) cmd);
        } else if (cmd instanceof DeleteObjectFromSwiftCommand) {
            return execute((DeleteObjectFromSwiftCommand) cmd);
        } else if (cmd instanceof CleanupSnapshotBackupCommand){
            return execute((CleanupSnapshotBackupCommand)cmd);
        } else {
            return Answer.createUnsupportedCommandAnswer(cmd);
        }
    }
    

    private Answer execute(downloadTemplateFromSwiftToSecondaryStorageCommand cmd) {
        SwiftTO swift = cmd.getSwift();
        String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
        Long accountId = cmd.getAccountId();
        Long templateId = cmd.getTemplateId();
        String path = cmd.getPath();
        String errMsg;
        String lDir = null;
        try {
            String parent = getRootDir(secondaryStorageUrl);
            lDir = parent + "/template/tmpl/" + accountId.toString() + "/" + templateId.toString();
            String result = createLocalDir(lDir);
            if (result != null) {
                errMsg = "downloadTemplateFromSwiftToSecondaryStorageCommand failed due to Create local directory failed";
                s_logger.warn(errMsg);
                throw new InternalErrorException(errMsg);
            }
            String lPath = lDir + "/" + path;
            result = swiftDownload(swift, "T-" + templateId.toString(), path, lPath);
            if (result != null) {
                errMsg = "failed to download template " + path + " from Swift to secondary storage " + lPath + " , err=" + result;
                s_logger.warn(errMsg);
                throw new CloudRuntimeException(errMsg);
            }
            path = "template.properties";
            lPath = lDir + "/" + path;
            result = swiftDownload(swift, "T-" + templateId.toString(), path, lPath);
            if (result != null) {
                errMsg = "failed to download template " + path + " from Swift to secondary storage " + lPath + " , err=" + result;
                s_logger.warn(errMsg);
                throw new CloudRuntimeException(errMsg);
            }
            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            if (lDir != null) {
                deleteLocalDir(lDir);
            }
            errMsg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(errMsg, e);
            return new Answer(cmd, false, errMsg);
        }
    }

    private Answer execute(uploadTemplateToSwiftFromSecondaryStorageCommand cmd) {
        SwiftTO swift = cmd.getSwift();
        String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
        Long accountId = cmd.getAccountId();
        Long templateId = cmd.getTemplateId();
        try {
            String parent = getRootDir(secondaryStorageUrl);
            String lPath = parent + "/template/tmpl/" + accountId.toString() + "/" + templateId.toString();
            if (!_storage.isFile(lPath + "/template.properties")) {
                String errMsg = cmd + " Command failed due to template doesn't exist ";
                s_logger.debug(errMsg);
                return new Answer(cmd, false, errMsg);
            }
            String result = swiftUpload(swift, "T-" + templateId.toString(), lPath, "*");
            if (result != null) {
                String errMsg = "failed to upload template from secondary storage " + lPath + " to swift  , err=" + result;
                s_logger.debug(errMsg);
                return new Answer(cmd, false, errMsg);
            }
            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            String errMsg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(errMsg, e);
            return new Answer(cmd, false, errMsg);
        }
    }

    private Answer execute(DeleteObjectFromSwiftCommand cmd) {
        SwiftTO swift = cmd.getSwift();
        String container = cmd.getContainer();
        String object = cmd.getObject();
        if (object == null) {
            object = "";
        }
        try {
            String result = swiftDelete(swift, container, object);
            if (result != null) {
                String errMsg = "failed to delete object " + container + "/" + object + " , err=" + result;
                s_logger.warn(errMsg);
                return new Answer(cmd, false, errMsg);
            }
            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            String errMsg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(errMsg, e);
            return new Answer(cmd, false, errMsg);
        }

    }

    String swiftDownload(SwiftTO swift, String container, String rfilename, String lFullPath) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A "
                + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName() + " -K " + swift.getKey()
                + " download " + container + " " + rfilename + " -o " + lFullPath);
        OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        String result = command.execute(parser);
        if (result != null) {
            String errMsg = "swiftDownload failed  err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        if (parser.getLines() != null) {
            String[] lines = parser.getLines().split("\\n");
            for (String line : lines) {
                if (line.contains("Errno") || line.contains("failed")) {
                    String errMsg = "swiftDownload failed , err=" + lines.toString();
                    s_logger.warn(errMsg);
                    return errMsg;
                }
            }
        }
        return null;

    }

    String swiftDownloadContainer(SwiftTO swift, String container, String ldir) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("cd " + ldir + ";/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A " + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName() + " -K "
                + swift.getKey() + " download " + container);
        OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        String result = command.execute(parser);
        if (result != null) {
            String errMsg = "swiftDownloadContainer failed  err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        if (parser.getLines() != null) {
            String[] lines = parser.getLines().split("\\n");
            for (String line : lines) {
                if (line.contains("Errno") || line.contains("failed")) {
                    String errMsg = "swiftDownloadContainer failed , err=" + lines.toString();
                    s_logger.warn(errMsg);
                    return errMsg;
                }
            }
        }
        return null;

    }

    String swiftUpload(SwiftTO swift, String container, String lDir, String lFilename) {
        long SWIFT_MAX_SIZE = 5L * 1024L * 1024L * 1024L;
        List<String> files = new ArrayList<String>();
        if (lFilename.equals("*")) {
            File dir = new File(lDir);
            for (String file : dir.list()) {
                if (file.startsWith(".")) {
                    continue;
                }
                files.add(file);
            }
        } else {
            files.add(lFilename);
        }

        for (String file : files) {
            File f = new File(lDir + "/" + file);
            long size = f.length();
            Script command = new Script("/bin/bash", s_logger);
            command.add("-c");
            if (size <= SWIFT_MAX_SIZE) {
                command.add("cd " + lDir + ";/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A " + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName()
                        + " -K " + swift.getKey() + " upload " + container + " " + file);
            } else {
                command.add("cd " + lDir + ";/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A " + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName()
                        + " -K " + swift.getKey() + " upload -S " + SWIFT_MAX_SIZE + " " + container + " " + file);
            }
            OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
            String result = command.execute(parser);
            if (result != null) {
                String errMsg = "swiftUpload failed , err=" + result;
                s_logger.warn(errMsg);
                return errMsg;
            }
            if (parser.getLines() != null) {
                String[] lines = parser.getLines().split("\\n");
                for (String line : lines) {
                    if (line.contains("Errno") || line.contains("failed")) {
                        String errMsg = "swiftUpload failed , err=" + lines.toString();
                        s_logger.warn(errMsg);
                        return errMsg;
                    }
                }
            }
        }

        return null;
    }

    String[] swiftList(SwiftTO swift, String container, String rFilename) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A " + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName() + " -K "
                + swift.getKey() + " list " + container + " " + rFilename);
        OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        String result = command.execute(parser);
        if (result == null && parser.getLines() != null) {
            String[] lines = parser.getLines().split("\\n");
            return lines;
        } else {
            if (result != null) {
                String errMsg = "swiftList failed , err=" + result;
                s_logger.warn(errMsg);
            } else {
                String errMsg = "swiftList failed, no lines returns";
                s_logger.warn(errMsg);
            }
        }
        return null;
    }

    String swiftDelete(SwiftTO swift, String container, String object) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("/usr/bin/python /usr/local/cloud/systemvm/scripts/storage/secondary/swift -A "
                + swift.getUrl() + " -U " + swift.getAccount() + ":" + swift.getUserName() + " -K " + swift.getKey()
 + " delete " + container + " " + object);
        OutputInterpreter.AllLinesParser parser = new OutputInterpreter.AllLinesParser();
        String result = command.execute(parser);
        if (result != null) {
            String errMsg = "swiftDelete failed , err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        if (parser.getLines() != null) {
            String[] lines = parser.getLines().split("\\n");
            for (String line : lines) {
                if (line.contains("Errno") || line.contains("failed")) {
                    String errMsg = "swiftDelete failed , err=" + lines.toString();
                    s_logger.warn(errMsg);
                    return errMsg;
                }
            }
        }
        return null;
    }


    public Answer execute(DeleteSnapshotsDirCommand cmd){
        String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        try {
            String parent = getRootDir(secondaryStorageUrl);
            String lPath = parent + "/snapshots/" + String.valueOf(accountId) + "/" + String.valueOf(volumeId) + "/*";
            String result = deleteLocalFile(lPath);
            if (result != null) {
                String errMsg = "failed to delete all snapshots " + lPath + " , err=" + result;
                s_logger.warn(errMsg);
                return new Answer(cmd, false, errMsg);
            }
            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            String errMsg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(errMsg, e);
            return new Answer(cmd, false, errMsg);
        }
    }

    public Answer execute(downloadSnapshotFromSwiftCommand cmd){
        SwiftTO swift = cmd.getSwift();
        String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String rFilename = cmd.getSnapshotUuid();
        String sParent = cmd.getParent();
        String errMsg = "";
        try {
            String parent = getRootDir(secondaryStorageUrl);
            String lPath = parent + "/snapshots/" + String.valueOf(accountId) + "/" + String.valueOf(volumeId);

            String result = createLocalDir(lPath);
            if ( result != null ) {
                errMsg = "downloadSnapshotFromSwiftCommand failed due to Create local path failed";
                s_logger.warn(errMsg);
                throw new InternalErrorException(errMsg);
            }
            String lFilename = rFilename;
            if ( rFilename.startsWith("VHD-") ) {
                lFilename = rFilename.replace("VHD-", "") + ".vhd";
            }
            String lFullPath = lPath + "/" + lFilename;
            result = swiftDownload(swift, "S-" + volumeId.toString(), rFilename, lFullPath);
            if (result != null) {
                return new Answer(cmd, false, result);
            }
            if (sParent != null) {
                if (sParent.startsWith("VHD-") || sParent.endsWith(".vhd")) {
                    String pFilename = sParent;
                    if (sParent.startsWith("VHD-")) {
                        pFilename = pFilename.replace("VHD-", "") + ".vhd";
                    }
                    String pFullPath = lPath + "/" + pFilename;
                    result = setVhdParent(lFullPath, pFullPath);
                    if (result != null) {
                        return new Answer(cmd, false, result);
                    }
                }
            }

            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            String msg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(msg, e);
            throw new CloudRuntimeException(msg);
        }
    }

    private Answer execute(ComputeChecksumCommand cmd) {
        
        String relativeTemplatePath = cmd.getTemplatePath();
        String parent = getRootDir(cmd);

        if (relativeTemplatePath.startsWith(File.separator)) {
            relativeTemplatePath = relativeTemplatePath.substring(1);
        }

        if (!parent.endsWith(File.separator)) {
            parent += File.separator;
        }
        String absoluteTemplatePath = parent + relativeTemplatePath;
        MessageDigest digest;
        String checksum = null;
        File f = new File(absoluteTemplatePath);   
        InputStream is = null;
        byte[] buffer = new byte[8192];
        int read = 0;
        if(s_logger.isDebugEnabled()){
            s_logger.debug("parent path " +parent+ " relative template path " +relativeTemplatePath );   
        }
        
        
        try {
            digest = MessageDigest.getInstance("MD5");           
            is = new FileInputStream(f);     
            while( (read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }       
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            checksum = bigInt.toString(16);
            if(s_logger.isDebugEnabled()){
                s_logger.debug("Successfully calculated checksum for file " +absoluteTemplatePath+ " - " +checksum );   
            }
        
        }catch(IOException e) {
            String logMsg = "Unable to process file for MD5 - " + absoluteTemplatePath;
            s_logger.error(logMsg);
            return new Answer(cmd, false, checksum); 
        }catch (NoSuchAlgorithmException e) {         
            return new Answer(cmd, false, checksum);
        }
        finally {
            try {
            	if(is != null)
            		is.close();
            } catch (IOException e) {
                if(s_logger.isDebugEnabled()){
                  s_logger.debug("Could not close the file " +absoluteTemplatePath);   
                }
                return new Answer(cmd, false, checksum);   
            }                        
        }

        return new Answer(cmd, true, checksum);
    }

    private void configCerts(Certificates certs) {
    	if (certs == null) {
    		configureSSL();
    	} else {
    		String prvKey = certs.getPrivKey();
    		String pubCert = certs.getPrivCert();
    		String certChain = certs.getCertChain();
    		
    		try {
				File prvKeyFile = File.createTempFile("prvkey", null);
				String prvkeyPath = prvKeyFile.getAbsolutePath();
				BufferedWriter out = new BufferedWriter(new FileWriter(prvKeyFile));
				out.write(prvKey);
				out.close();
				
				File pubCertFile = File.createTempFile("pubcert", null);
				String pubCertFilePath = pubCertFile.getAbsolutePath();
				
				out = new BufferedWriter(new FileWriter(pubCertFile));
				out.write(pubCert);
				out.close();
				
				configureSSL(prvkeyPath, pubCertFilePath, null);
				
				prvKeyFile.delete();
				pubCertFile.delete();
				
			} catch (IOException e) {
				s_logger.debug("Failed to config ssl: " + e.toString());
			}
    	}
    }
    
    private Answer execute(SecStorageSetupCommand cmd) {
        if (!_inSystemVM){
            return new Answer(cmd, true, null);
        }
        String secUrl = cmd.getSecUrl();
        try {
            URI uri = new URI(secUrl);
            String nfsHost = uri.getHost();

            InetAddress nfsHostAddr = InetAddress.getByName(nfsHost);
            String nfsHostIp = nfsHostAddr.getHostAddress();

            addRouteToInternalIpOrCidr(_storageGateway, _storageIp, _storageNetmask, nfsHostIp);
            String nfsPath = nfsHostIp + ":" + uri.getPath();
            String dir = UUID.nameUUIDFromBytes(nfsPath.getBytes()).toString();
            String root = _parent + "/" + dir;
            mount(root, nfsPath);

            configCerts(cmd.getCerts());
            
            return new SecStorageSetupAnswer(dir);
        } catch (Exception e) {
            String msg = "GetRootDir for " + secUrl + " failed due to " + e.toString();
            s_logger.error(msg);
            return new Answer(cmd, false, msg);

        }
    }
    
    protected Answer execute(final DeleteSnapshotBackupCommand cmd) {
        String secondaryStorageUrl = cmd.getSecondaryStorageUrl();
        Long accountId = cmd.getAccountId();
        Long volumeId = cmd.getVolumeId();
        String name = cmd.getSnapshotUuid();
        try {
            SwiftTO swift = cmd.getSwift();
            if (swift == null) {
                String parent = getRootDir(secondaryStorageUrl);
                String filename;
                if (cmd.isAll()) {
                    filename = "*";

                } else {
                    filename = "*" + name + "*";
                }
                String lPath = parent + "/snapshots/" + String.valueOf(accountId) + "/" + String.valueOf(volumeId) + "/" + filename;
                String result = deleteLocalFile(lPath);
                if (result != null) {
                    String errMsg = "failed to delete snapshot " + lPath + " , err=" + result;
                    s_logger.warn(errMsg);
                    return new Answer(cmd, false, errMsg);
                }
            } else {
                String filename;
                if (cmd.isAll()) {
                    filename = "";
                } else {
                    filename = name;
                }
                String result = swiftDelete(swift, "V-" + volumeId.toString(), filename);
                if (result != null) {
                    String errMsg = "failed to delete snapshot " + filename + " , err=" + result;
                    s_logger.warn(errMsg);
                    return new Answer(cmd, false, errMsg);
                }
            }
            return new Answer(cmd, true, "success");
        } catch (Exception e) {
            String errMsg = cmd + " Command failed due to " + e.toString();
            s_logger.warn(errMsg, e);
            return new Answer(cmd, false, errMsg);
        }
    }
    
    Map<String, TemplateInfo> swiftListTemplate(SwiftTO swift) {
        String[] containers = swiftList(swift, "", "");
        if (containers == null) {
            return null;
        }
        Map<String, TemplateInfo> tmpltInfos = new HashMap<String, TemplateInfo>();
        for( String container : containers) {
            if ( container.startsWith("T-")) {
                String ldir = _tmpltDir + "/" + UUID.randomUUID().toString();
                createLocalDir(ldir);
                String lFullPath = ldir + "/" + _tmpltpp;
                swiftDownload(swift, container, _tmpltpp, lFullPath);
                TemplateLocation loc = new TemplateLocation(_storage, ldir);
                try {
                    if (!loc.load()) {
                        s_logger.warn("Can not parse template.properties file for template " + container);
                        continue;
                    }
                } catch (IOException e) {
                    s_logger.warn("Unable to load template location " + ldir + " due to " + e.toString(), e);
                    continue;
                }
                TemplateInfo tInfo = loc.getTemplateInfo();
                tInfo.setInstallPath(container);
                tmpltInfos.put(tInfo.getTemplateName(), tInfo);
                loc.purge();
                deleteLocalDir(ldir);
            }
        }
        return tmpltInfos;
        
    }
    
    private Answer execute(ListTemplateCommand cmd) {
        if (!_inSystemVM){
            return new Answer(cmd, true, null);
        }
        if (cmd.getSwift() != null) {
            Map<String, TemplateInfo> templateInfos = swiftListTemplate(cmd.getSwift());
            return new ListTemplateAnswer(cmd.getSwift().toString(), templateInfos);
        } else {
            String root = getRootDir(cmd.getSecUrl());
            Map<String, TemplateInfo> templateInfos = _dlMgr.gatherTemplateInfo(root);
            return new ListTemplateAnswer(cmd.getSecUrl(), templateInfos);
        }
    }
    
    private Answer execute(SecStorageVMSetupCommand cmd) {
    	if (!_inSystemVM){
			return new Answer(cmd, true, null);
		}
		boolean success = true;
		StringBuilder result = new StringBuilder();
		for (String cidr: cmd.getAllowedInternalSites()) {
			String tmpresult = allowOutgoingOnPrivate(cidr);
			if (tmpresult != null) {
				result.append(", ").append(tmpresult);
				success = false;
			}
		}
		if (success) {
			if (cmd.getCopyPassword() != null && cmd.getCopyUserName() != null) {
				String tmpresult = configureAuth(cmd.getCopyUserName(), cmd.getCopyPassword());
				if (tmpresult != null) {
					result.append("Failed to configure auth for copy ").append(tmpresult);
					success = false;
				}
			}
		}
		return new Answer(cmd, success, result.toString());

	}

    private String setVhdParent(String lFullPath, String pFullPath) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("/bin/vhd-util modify -n " + lFullPath + " -p " + pFullPath);
        String result = command.execute();
        if (result != null) {
            String errMsg = "failed to set vhd parent, child " + lFullPath + " parent " + pFullPath + ", err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        return null;
    }

    private String createLocalDir(String folder) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("mkdir -p " + folder);
        String result = command.execute();
        if (result != null) {
            String errMsg = "Create local path " + folder + " failed , err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        return null;
    }

    private String deleteLocalDir(String folder) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("rmdir " + folder);
        String result = command.execute();
        if (result != null) {
            String errMsg = "Delete local path " + folder + " failed , err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        return null;
    }

    private String deleteLocalFile(String fullPath) {
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("rm -f " + fullPath);
        String result = command.execute();
        if (result != null) {
            String errMsg = "Failed to delete file " + fullPath + ", err=" + result;
            s_logger.warn(errMsg);
            return errMsg;
        }
        return null;
    }

    public String allowOutgoingOnPrivate(String destCidr) {
    	
    	Script command = new Script("/bin/bash", s_logger);
    	String intf = "eth1";
    	command.add("-c");
    	command.add("iptables -I OUTPUT -o " + intf + " -d " + destCidr + " -p tcp -m state --state NEW -m tcp  -j ACCEPT");

    	String result = command.execute();
    	if (result != null) {
    		s_logger.warn("Error in allowing outgoing to " + destCidr + ", err=" + result );
    		return "Error in allowing outgoing to " + destCidr + ", err=" + result;
    	}
    	addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, destCidr);
    	return null;
	}
    
	private Answer execute(SecStorageFirewallCfgCommand cmd) {
		if (!_inSystemVM){
			return new Answer(cmd, true, null);
		}

		List<String> ipList = new ArrayList<String>();
		
		for (PortConfig pCfg:cmd.getPortConfigs()){
			if (pCfg.isAdd()) {
				ipList.add(pCfg.getSourceIp());		
			}
		}
		boolean success = true;
		String result;
		result = configureIpFirewall(ipList, cmd.getIsAppendAIp());
		if (result !=null)
			success = false;

		return new Answer(cmd, success, result);
	}

	protected GetStorageStatsAnswer execute(final GetStorageStatsCommand cmd) {
	    String rootDir = getRootDir(cmd.getSecUrl());
        final long usedSize = getUsedSize(rootDir);
        final long totalSize = getTotalSize(rootDir);
        if (usedSize == -1 || totalSize == -1) {
        	return new GetStorageStatsAnswer(cmd, "Unable to get storage stats");
        } else {
        	return new GetStorageStatsAnswer(cmd, totalSize, usedSize) ;
        }
    }
    
    protected Answer execute(final DeleteTemplateCommand cmd) {
        String relativeTemplatePath = cmd.getTemplatePath();
        String parent = getRootDir(cmd);

        if (relativeTemplatePath.startsWith(File.separator)) {
            relativeTemplatePath = relativeTemplatePath.substring(1);
        }

        if (!parent.endsWith(File.separator)) {
            parent += File.separator;
        }
        String absoluteTemplatePath = parent + relativeTemplatePath;
        File tmpltParent = new File(absoluteTemplatePath).getParentFile();
        String details = null;
        if (!tmpltParent.exists()) {
            details = "template parent directory " + tmpltParent.getName() + " doesn't exist";
            s_logger.debug(details);
            return new Answer(cmd, true, details);
        }
        File[] tmpltFiles = tmpltParent.listFiles();
        if (tmpltFiles == null || tmpltFiles.length == 0) {
            details = "No files under template parent directory " + tmpltParent.getName();
            s_logger.debug(details);
        } else {
            boolean found = false;
            for (File f : tmpltFiles) {
                if (!found && f.getName().equals("template.properties")) {
                    found = true;
                }
                if (!f.delete()) {
                    return new Answer(cmd, false, "Unable to delete file " + f.getName() + " under Template path "
                            + relativeTemplatePath);
                }
            }
            if (!found) {
                details = "Can not find template.properties under " + tmpltParent.getName();
                s_logger.debug(details);
            }
        }
        if (!tmpltParent.delete()) {
            details = "Unable to delete directory " + tmpltParent.getName() + " under Template path "
                    + relativeTemplatePath;
            s_logger.debug(details);
            return new Answer(cmd, false, details);
        }
        return new Answer(cmd, true, null);
    }
    
    Answer execute(CleanupSnapshotBackupCommand cmd) {
        String parent = getRootDir(cmd.getSecondaryStoragePoolURL());
        if (!parent.endsWith(File.separator)) {
            parent += File.separator;
        }
        String absoluteSnapsthotDir = parent + File.separator + "snapshots" + File.separator + cmd.getAccountId() + File.separator + cmd.getVolumeId();
        File ssParent = new File(absoluteSnapsthotDir);
        if (ssParent.exists() && ssParent.isDirectory()) {
            File[] files = ssParent.listFiles();
            for (File file : files) {
                boolean found = false;
                String filename = file.getName();
                for (String uuid : cmd.getValidBackupUUIDs()) {
                    if (filename.startsWith(uuid)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    file.delete();
                    String msg = "snapshot " + filename + " is not recorded in DB, remove it";
                    s_logger.warn(msg);
                }
            }
        }
        return new Answer(cmd, true, null);
    }


    synchronized public String getRootDir(String secUrl) {
        try {
            URI uri = new URI(secUrl);
            String nfsHost = uri.getHost();

            InetAddress nfsHostAddr = InetAddress.getByName(nfsHost);
            String nfsHostIp = nfsHostAddr.getHostAddress();
            String nfsPath = nfsHostIp + ":" + uri.getPath();
            String dir = UUID.nameUUIDFromBytes(nfsPath.getBytes()).toString();
            String root = _parent + "/" + dir;
            mount(root, nfsPath);    
            return root;
        } catch (Exception e) {
            String msg = "GetRootDir for " + secUrl + " failed due to " + e.toString();
            s_logger.error(msg, e);
            throw new CloudRuntimeException(msg);
        }
    }
    
    
    @Override
    public String getRootDir(ssCommand cmd){
        return getRootDir(cmd.getSecUrl());
        
    }
    
    protected long getUsedSize(String rootDir) {
        return _storage.getUsedSpace(rootDir);
    }
    
    protected long getTotalSize(String rootDir) {
      	return _storage.getTotalSpace(rootDir);
    }
    
    protected long convertFilesystemSize(final String size) {
        if (size == null || size.isEmpty()) {
            return -1;
        }

        long multiplier = 1;
        if (size.endsWith("T")) {
            multiplier = 1024l * 1024l * 1024l * 1024l;
        } else if (size.endsWith("G")) {
            multiplier = 1024l * 1024l * 1024l;
        } else if (size.endsWith("M")) {
            multiplier = 1024l * 1024l;
        } else {
            assert (false) : "Well, I have no idea what this is: " + size;
        }

        return (long)(Double.parseDouble(size.substring(0, size.length() - 1)) * multiplier);
    }
    

    @Override
    public Type getType() {
    	if(SecondaryStorageVm.Role.templateProcessor.toString().equals(_role))
    		return Host.Type.SecondaryStorage;
    	
    	return Host.Type.SecondaryStorageCmdExecutor;
    }
    
    @Override
    public PingCommand getCurrentStatus(final long id) {
        return new PingStorageCommand(Host.Type.Storage, id, new HashMap<String, Boolean>());
    }
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
    	_eth1ip = (String)params.get("eth1ip");
        _eth1mask = (String)params.get("eth1mask");
        if (_eth1ip != null) { //can only happen inside service vm
        	params.put("private.network.device", "eth1");
        } else {
        	s_logger.warn("Wait, what's going on? eth1ip is null!!");
        }
        String eth2ip = (String) params.get("eth2ip");
        if (eth2ip != null) {
            params.put("public.network.device", "eth2");
        }         
        _publicIp = (String) params.get("eth2ip");
        _hostname = (String) params.get("name");
        
        _storageIp = (String) params.get("storageip");
        if (_storageIp == null) {
        	s_logger.warn("Wait, there is no storageip in /proc/cmdline, something wrong!");
        }
        _storageNetmask = (String) params.get("storagenetmask");
        _storageGateway = (String) params.get("storagegateway");
        super.configure(name, params);
        
        _params = params;
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
        _storage.mkdirs(_parent);
        _configSslScr = Script.findScript(getDefaultScriptsDir(), "config_ssl.sh");
        if (_configSslScr != null) {
            s_logger.info("config_ssl.sh found in " + _configSslScr);
        }

        _configAuthScr = Script.findScript(getDefaultScriptsDir(), "config_auth.sh");
        if (_configSslScr != null) {
            s_logger.info("config_auth.sh found in " + _configAuthScr);
        }
        
        _configIpFirewallScr = Script.findScript(getDefaultScriptsDir(), "ipfirewall.sh");
        if (_configIpFirewallScr != null) {
            s_logger.info("_configIpFirewallScr found in " + _configIpFirewallScr);
        }
        
        _role = (String)params.get("role");
        if(_role == null)
        	_role = SecondaryStorageVm.Role.templateProcessor.toString();
        s_logger.info("Secondary storage runs in role " + _role);
        
        _guid = (String)params.get("guid");
        if (_guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }
        
        _dc = (String)params.get("zone");
        if (_dc == null) {
            throw new ConfigurationException("Unable to find the zone");
        }
        _pod = (String)params.get("pod");
        
        _instance = (String)params.get("instance");
    
        
        String inSystemVM = (String)params.get("secondary.storage.vm");
        if (inSystemVM == null || "true".equalsIgnoreCase(inSystemVM)) {
        	_inSystemVM = true;
            _localgw = (String)params.get("localgw");
            if (_localgw != null) { // can only happen inside service vm
                String mgmtHost = (String) params.get("host");
                addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, mgmtHost);

                String internalDns1 = (String) params.get("internaldns1");
                if (internalDns1 == null) {
                    s_logger.warn("No DNS entry found during configuration of NfsSecondaryStorage");
                } else {
                    addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, internalDns1);
                }

                String internalDns2 = (String) params.get("internaldns2");
                if (internalDns2 != null) {
                    addRouteToInternalIpOrCidr(_localgw, _eth1ip, _eth1mask, internalDns2);
                }

            }
            
        	startAdditionalServices();
        	_params.put("install.numthreads", "50");
        	_params.put("secondary.storage.vm", "true");
        }
        
        try {
            _params.put(StorageLayer.InstanceConfigKey, _storage);
            _dlMgr = new DownloadManagerImpl();
            _dlMgr.configure("DownloadManager", _params);
            _upldMgr = new UploadManagerImpl();
            _upldMgr.configure("UploadManager", params);
        } catch (ConfigurationException e) {
            s_logger.warn("Caught problem while configuring DownloadManager", e);
            return false;
        }
        return true;
    }
    
    private void startAdditionalServices() {
    	Script command = new Script("/bin/bash", s_logger);
		command.add("-c");
    	command.add("if [ -f /etc/init.d/ssh ]; then service ssh restart; else service sshd restart; fi ");
    	String result = command.execute();
    	if (result != null) {
    		s_logger.warn("Error in starting sshd service err=" + result );
    	}
		command = new Script("/bin/bash", s_logger);
		command.add("-c");
    	command.add("iptables -I INPUT -i eth1 -p tcp -m state --state NEW -m tcp --dport 3922 -j ACCEPT");
    	result = command.execute();
    	if (result != null) {
    		s_logger.warn("Error in opening up ssh port err=" + result );
    	}
	}
    
    private void addRouteToInternalIpOrCidr(String localgw, String eth1ip, String eth1mask, String destIpOrCidr) {
    	s_logger.debug("addRouteToInternalIp: localgw=" + localgw + ", eth1ip=" + eth1ip + ", eth1mask=" + eth1mask + ",destIp=" + destIpOrCidr);
    	if (destIpOrCidr == null) {
    		s_logger.debug("addRouteToInternalIp: destIp is null");
			return;
		}
    	if (!NetUtils.isValidIp(destIpOrCidr) && !NetUtils.isValidCIDR(destIpOrCidr)){
    		s_logger.warn(" destIp is not a valid ip address or cidr destIp=" + destIpOrCidr);
    		return;
    	}
    	boolean inSameSubnet = false;
    	if (NetUtils.isValidIp(destIpOrCidr)) {
    		if (eth1ip != null && eth1mask != null) {
    			inSameSubnet = NetUtils.sameSubnet(eth1ip, destIpOrCidr, eth1mask);
    		} else {
    			s_logger.warn("addRouteToInternalIp: unable to determine same subnet: _eth1ip=" + eth1ip + ", dest ip=" + destIpOrCidr + ", _eth1mask=" + eth1mask);
    		}
    	} else {
            inSameSubnet = NetUtils.isNetworkAWithinNetworkB(destIpOrCidr, NetUtils.ipAndNetMaskToCidr(eth1ip, eth1mask));
    	}
    	if (inSameSubnet) {
			s_logger.debug("addRouteToInternalIp: dest ip " + destIpOrCidr + " is in the same subnet as eth1 ip " + eth1ip);
    		return;
    	}
    	Script command = new Script("/bin/bash", s_logger);
		command.add("-c");
    	command.add("ip route delete " + destIpOrCidr);
    	command.execute();
		command = new Script("/bin/bash", s_logger);
		command.add("-c");
    	command.add("ip route add " + destIpOrCidr + " via " + localgw);
    	String result = command.execute();
    	if (result != null) {
    		s_logger.warn("Error in configuring route to internal ip err=" + result );
    	} else {
			s_logger.debug("addRouteToInternalIp: added route to internal ip=" + destIpOrCidr + " via " + localgw);
    	}
    }

	private void configureSSL() {
		Script command = new Script(_configSslScr);
		command.add("-i", _publicIp);
		command.add("-h", _hostname);
		String result = command.execute();
		if (result != null) {
			s_logger.warn("Unable to configure httpd to use ssl");
		}
	}
	
	private void configureSSL(String prvkeyPath, String prvCertPath, String certChainPath) {
		Script command = new Script(_configSslScr);
		command.add("-i", _publicIp);
		command.add("-h", _hostname);
		command.add("-k", prvkeyPath);
		command.add("-p", prvCertPath);
		if (certChainPath != null) {
			command.add("-t", certChainPath);
		}
		String result = command.execute();
		if (result != null) {
			s_logger.warn("Unable to configure httpd to use ssl");
		}
	}
	
	private String configureAuth(String user, String passwd) {
		Script command = new Script(_configAuthScr);
		command.add(user);
		command.add(passwd);
		String result = command.execute();
		if (result != null) {
			s_logger.warn("Unable to configure httpd to use auth");
		}
		return result;
	}
	
	private String configureIpFirewall(List<String> ipList, boolean isAppend){
		Script command = new Script(_configIpFirewallScr);
		command.add(String.valueOf(isAppend));
		for (String ip : ipList){
			command.add(ip);
		}		
		
		String result = command.execute();
		if (result != null) {
			s_logger.warn("Unable to configure firewall for command : " +command);
		}
		return result;
	}
	
	protected String mount(String root, String nfsPath) {
        File file = new File(root);
        if (!file.exists()) {
            if (_storage.mkdir(root)) {
                s_logger.debug("create mount point: " + root);
            } else {
                s_logger.debug("Unable to create mount point: " + root);
                return null;       
            }
	    }
       
        Script script = null;
        String result = null;
        script = new Script(!_inSystemVM, "mount", _timeout, s_logger);
        List<String> res = new ArrayList<String>();
        ZfsPathParser parser = new ZfsPathParser(root);
        script.execute(parser);
        res.addAll(parser.getPaths());
        for( String s : res ) {
            if ( s.contains(root)) {
                return root;
            }
        }
            
        Script command = new Script(!_inSystemVM, "mount", _timeout, s_logger);
        command.add("-t", "nfs");
        if (_inSystemVM) {
        	//Fedora Core 12 errors out with any -o option executed from java
        	command.add("-o", "soft,timeo=133,retrans=2147483647,tcp,acdirmax=0,acdirmin=0");
        }
        command.add(nfsPath);
        command.add(root);
        result = command.execute();
        if (result != null) {
            s_logger.warn("Unable to mount " + nfsPath + " due to " + result);
            file = new File(root);
            if (file.exists())
            	file.delete();
            return null;
        }
        
        // XXX: Adding the check for creation of snapshots dir here. Might have to move it somewhere more logical later.
        if (!checkForSnapshotsDir(root)) {
        	return null;
        }
        
        // Create the volumes dir
        if (!checkForVolumesDir(root)) {
        	return null;
        }
        
        return root;
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
    public StartupCommand[] initialize() {
        
        final StartupSecondaryStorageCommand cmd = new StartupSecondaryStorageCommand();
        fillNetworkInformation(cmd);
        if(_publicIp != null)
            cmd.setPublicIpAddress(_publicIp);
        
        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("ln -sf " + _parent + " /var/www/html/copy");
        String result = command.execute();
        if (result != null) {
            s_logger.warn("Error in linking  err=" + result);
            return null;
        }
        return new StartupCommand[] {cmd};
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
    
    @Override
    protected String getDefaultScriptsDir() {
        return "./scripts/storage/secondary";
    }
}
