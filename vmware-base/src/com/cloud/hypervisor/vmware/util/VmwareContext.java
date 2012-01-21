/**
 *  Copyright (C) 2011 Cloud.com, Inc.  All rights reserved.
 */

package com.cloud.hypervisor.vmware.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.vmware.mo.DatacenterMO;
import com.cloud.hypervisor.vmware.mo.DatastoreFile;
import com.cloud.utils.ActionDelegate;
import com.vmware.apputils.version.ExtendedAppUtil;
import com.vmware.apputils.vim25.ServiceConnection;
import com.vmware.apputils.vim25.ServiceUtil;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

public class VmwareContext {
    private static final Logger s_logger = Logger.getLogger(VmwareContext.class);

    private static int MAX_CONNECT_RETRY = 5;
    private static int CONNECT_RETRY_INTERVAL = 1000;
    
	private ExtendedAppUtil _appUtil;
	private String _serverAddress;
	
	private Map<String, Object> _stockMap = new HashMap<String, Object>();
	private int _CHUNKSIZE = 1*1024*1024;		// 1M
	
	
	static {
		try {
			javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1]; 
			javax.net.ssl.TrustManager tm = new TrustAllManager(); 
			trustAllCerts[0] = tm; 
			javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL"); 
			sc.init(null, trustAllCerts, null); 
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			s_logger.error("Unexpected exception ", e);
		}
	}
	
	public VmwareContext(ExtendedAppUtil appUtil, String address) {
		assert(appUtil != null) : "Invalid parameter in constructing VmwareContext object";
		
		_appUtil = appUtil;
		_serverAddress = address;
	}

	public void registerStockObject(String name, Object obj) {
		synchronized(_stockMap) {
			_stockMap.put(name, obj);
		}
	}
	
	public void uregisterStockObject(String name) {
		synchronized(_stockMap) {
			_stockMap.remove(name);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getStockObject(String name) {
		synchronized(_stockMap) {
			return (T)_stockMap.get(name);
		}
	}
	
	public String getServerAddress() {
		return _serverAddress;
	}
	
	public ServiceConnection getServiceConnection() {
		return _appUtil.getServiceConnection3();
	}
	
	public VimPortType getService() {
		return getServiceConnection().getService();
	}
	
	public ServiceContent getServiceContent() {
		return getServiceConnection().getServiceContent();
	}
	
	public ServiceUtil getServiceUtil() {
		return _appUtil.getServiceUtil3(); 
	}
	
	public ManagedObjectReference getRootFolder() {
		return getServiceContent().getRootFolder();
	}
	
	public ManagedObjectReference getHostMorByPath(String inventoryPath) throws Exception {
		assert(inventoryPath != null);
		
		String[] tokens;
		if(inventoryPath.startsWith("/"))
			tokens = inventoryPath.substring(1).split("/");
		else
			tokens = inventoryPath.split("/");
		
		ManagedObjectReference mor = getRootFolder();
		for(int i=0; i < tokens.length;i++) {
			String token = tokens[i];
			ObjectContent[] ocs;
			if(mor.getType().equalsIgnoreCase("Datacenter")) {
				PropertySpec pSpec = new PropertySpec();
				pSpec.setType("ManagedEntity");
				pSpec.setPathSet(new String[] { "name" });
				
			    TraversalSpec dcHostFolderTraversal = new TraversalSpec();
			    dcHostFolderTraversal.setType("Datacenter");
			    dcHostFolderTraversal.setPath("hostFolder");
			    dcHostFolderTraversal.setName("dcHostFolderTraversal");

			    ObjectSpec oSpec = new ObjectSpec();
			    oSpec.setObj(mor);
			    oSpec.setSkip(Boolean.TRUE);
			    oSpec.setSelectSet(new SelectionSpec[] { dcHostFolderTraversal });

			    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
			    pfSpec.setPropSet(new PropertySpec[] { pSpec });
			    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
			    ocs = getService().retrieveProperties(
			    	getServiceContent().getPropertyCollector(), 
			    	new PropertyFilterSpec[] { pfSpec });
				
			} else if(mor.getType().equalsIgnoreCase("Folder")) {
				PropertySpec pSpec = new PropertySpec();
				pSpec.setType("ManagedEntity");
				pSpec.setPathSet(new String[] { "name" });
				
			    TraversalSpec folderChildrenTraversal = new TraversalSpec();
			    folderChildrenTraversal.setType("Folder");
			    folderChildrenTraversal.setPath("childEntity");
			    folderChildrenTraversal.setName("folderChildrenTraversal");

			    ObjectSpec oSpec = new ObjectSpec();
			    oSpec.setObj(mor);
			    oSpec.setSkip(Boolean.TRUE);
			    oSpec.setSelectSet(new SelectionSpec[] { folderChildrenTraversal });

			    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
			    pfSpec.setPropSet(new PropertySpec[] { pSpec });
			    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
			    
			    ocs = getService().retrieveProperties(
			    	getServiceContent().getPropertyCollector(), 
			    	new PropertyFilterSpec[] { pfSpec });
			} else if(mor.getType().equalsIgnoreCase("ClusterComputeResource")) {
				PropertySpec pSpec = new PropertySpec();
				pSpec.setType("ManagedEntity");
				pSpec.setPathSet(new String[] { "name" });
				
			    TraversalSpec clusterHostTraversal = new TraversalSpec();
			    clusterHostTraversal.setType("ClusterComputeResource");
			    clusterHostTraversal.setPath("host");
			    clusterHostTraversal.setName("folderChildrenTraversal");

			    ObjectSpec oSpec = new ObjectSpec();
			    oSpec.setObj(mor);
			    oSpec.setSkip(Boolean.TRUE);
			    oSpec.setSelectSet(new SelectionSpec[] { clusterHostTraversal });

			    PropertyFilterSpec pfSpec = new PropertyFilterSpec();
			    pfSpec.setPropSet(new PropertySpec[] { pSpec });
			    pfSpec.setObjectSet(new ObjectSpec[] { oSpec });
			    
			    ocs = getService().retrieveProperties(
			    	getServiceContent().getPropertyCollector(), 
			    	new PropertyFilterSpec[] { pfSpec });
			} else {
				s_logger.error("Invalid inventory path, path element can only be datacenter and folder");
				return null;
			}
			
		    if(ocs != null && ocs.length > 0) {
		    	boolean found = false;
		    	for(ObjectContent oc : ocs) {
		    		String name = oc.getPropSet()[0].getVal().toString();		    		
		    		if(name.equalsIgnoreCase(token) || name.equalsIgnoreCase("host")) {
		    			mor = oc.getObj();
		    			found  = true;
		    			if (name.equalsIgnoreCase("host"))
		    				i--;
		    			break;
		    		}
		    	}
		    	if(!found) {
					s_logger.error("Path element points to an un-existing inventory entity");
			    	return null;
		    	}
		    } else {
				s_logger.error("Path element points to an un-existing inventory entity");
		    	return null;
		    }
		}
		return mor;
	}

	// path in format of <datacenter name>/<datastore name>
	public ManagedObjectReference getDatastoreMorByPath(String inventoryPath) throws Exception {
		assert(inventoryPath != null);
		
		String[] tokens;
		if(inventoryPath.startsWith("/"))
			tokens = inventoryPath.substring(1).split("/");
		else
			tokens = inventoryPath.split("/");
		
		if(tokens == null || tokens.length != 2) {
			s_logger.error("Invalid datastore inventory path. path: " + inventoryPath);
			return null;
		}
			
		DatacenterMO dcMo = new DatacenterMO(this, tokens[0]);
		if(dcMo.getMor() == null) {
			s_logger.error("Unable to locate the datacenter specified in path: " + inventoryPath);
			return null;
		}
		
		return dcMo.findDatastore(tokens[1]);
	}
	
	public void waitForTaskProgressDone(ManagedObjectReference morTask) throws Exception {
		while(true) {
			TaskInfo tinfo = (TaskInfo)getServiceUtil().getDynamicProperty(morTask, "info");
			Integer progress = tinfo.getProgress();
			if(progress == null)
				break;
			
			if(progress.intValue() >= 100)
				break;
			
			Thread.sleep(1000);
		}
	}
	
	public void getFile(String urlString, String localFileFullName) throws Exception {
		HttpURLConnection conn = getHTTPConnection(urlString);
		
	    InputStream in = conn.getInputStream();
	    OutputStream out = new FileOutputStream(new File(localFileFullName));
	    byte[] buf = new byte[_CHUNKSIZE];
	    int len = 0;
	    while ((len = in.read(buf)) > 0) {
	    	out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();              
	}
	
	public void uploadFile(String urlString, String localFileFullName) throws Exception {
		uploadFile(urlString, new File(localFileFullName));
	}
	
	public void uploadFile(String urlString, File localFile) throws Exception {
		HttpURLConnection conn = getHTTPConnection(urlString, "PUT");
		OutputStream out = null;
		InputStream in = null;
		BufferedReader br = null;
		
		try {
		    out = conn.getOutputStream();
		    in = new FileInputStream(localFile);
		    byte[] buf = new byte[_CHUNKSIZE];
		    int len = 0;
		    while ((len = in.read(buf)) > 0) {
		    	out.write(buf, 0, len);
		    }
		    out.flush();
		    
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(s_logger.isTraceEnabled())
		    		s_logger.trace("Upload " + urlString + " response: " + line);
		    }
		} finally {
			if(in != null)
				in.close();
			
			if(out != null)
				out.close();
			
			if(br != null)
				br.close();
		}
	}
	
	public void uploadVmdkFile(String httpMethod, String urlString, String localFileName, 
		long totalBytesUpdated, ActionDelegate progressUpdater) throws Exception {
		
		HttpURLConnection conn = getRawHTTPConnection(urlString);
		
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		
		conn.setChunkedStreamingMode(_CHUNKSIZE);  
		conn.setRequestMethod(httpMethod);  
		conn.setRequestProperty("Connection", "Keep-Alive");  
		conn.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");  
		conn.setRequestProperty("Content-Length", Long.toString(new File(localFileName).length()));
		connectWithRetry(conn);
		
		BufferedOutputStream bos = null;
		BufferedInputStream is = null;
		try {
			bos = new BufferedOutputStream(conn.getOutputStream());  
			is = new BufferedInputStream(new FileInputStream(localFileName));  
			int bufferSize = _CHUNKSIZE;  
			byte[] buffer = new byte[bufferSize];  
			while (true) {  
				int bytesRead = is.read(buffer, 0, bufferSize);  
				if (bytesRead == -1) {  
					break;  
				}
				bos.write(buffer, 0, bytesRead);
				totalBytesUpdated += bytesRead;
				bos.flush();
				if(progressUpdater != null)
					progressUpdater.action(new Long(totalBytesUpdated));
			}
		 	bos.flush();
		} finally {
			if(is != null)
				is.close();
			if(bos != null)
				bos.close();
			
		 	conn.disconnect();
		}
	}
	
	public long downloadVmdkFile(String urlString, String localFileName, 
			long totalBytesDownloaded, ActionDelegate progressUpdater) throws Exception {
		HttpURLConnection conn = getRawHTTPConnection(urlString);
		
		String cookieString = getServiceCookie();
	    conn.setRequestProperty(org.apache.axis.transport.http.HTTPConstants.HEADER_COOKIE, cookieString);
	    conn.setDoInput(true);
	    conn.setDoOutput(true);  
	    conn.setAllowUserInteraction(true);
        connectWithRetry(conn);

    	long bytesWritten = 0;  
	    InputStream in = null;  
	    OutputStream out = null;  
	    try {
	    	in = conn.getInputStream();  
	    	out = new FileOutputStream(new File(localFileName));  
	    
	    	byte[] buf = new byte[_CHUNKSIZE];  
	    	int len = 0;  
	    	while ((len = in.read(buf)) > 0) {  
	    		out.write(buf, 0, len);  
	    		bytesWritten += len;
	    		totalBytesDownloaded += len;
	    	
	    		if(progressUpdater != null)
	    			progressUpdater.action(new Long(totalBytesDownloaded));
	    	}  
	    } finally {
	    	if(in != null)
	    		in.close();
	    	if(out != null)
	    		out.close();
	    	
	    	conn.disconnect();
	    }
	    return bytesWritten; 
	}
	
	public byte[] getResourceContent(String urlString) throws Exception {
		HttpURLConnection conn = getHTTPConnection(urlString);
	    InputStream in = conn.getInputStream();
	    
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buf = new byte[_CHUNKSIZE];
	    int len = 0;
	    while ((len = in.read(buf)) > 0) {
	    	out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();              
	    return out.toByteArray();
	}
	
	public void uploadResourceContent(String urlString, byte[] content) throws Exception {
		// vSphere does not support POST
		HttpURLConnection conn = getHTTPConnection(urlString, "PUT");

		OutputStream out = conn.getOutputStream();
		out.write(content);
		out.flush();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    while ((line = in.readLine()) != null) {
	    	if(s_logger.isTraceEnabled())
	    		s_logger.trace("Upload " + urlString + " response: " + line);
	    }
	    out.close();
	    in.close();
	}
	
	/*
	 * Sample content returned by query a datastore directory
	 * 
	 * Url for the query
	 * 	https://vsphere-1.lab.vmops.com/folder/Fedora-clone-test?dcPath=cupertino&dsName=NFS+datastore
	 *
	 * Returned conent from vSphere
	 * 
	    <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	    <html>
	      <head>
	        <meta http-equiv="content-type" content="text/html; charset=utf-8">
	        <title>Index of Fedora-clone-test on datastore NFS datastore in datacenter cupertino</title></head>
	      <body>
	    <h1>Index of Fedora-clone-test on datastore NFS datastore in datacenter cupertino</h1>
	    <table>
		    <tr><th>Name</th><th>Last modified</th><th>Size</th></tr><tr><th colspan="3"><hr></th></tr>
		    <tr><td><a href="/folder?dcPath=cupertino&amp;dsName=NFS%20datastore">Parent Directory</a></td><td>&nbsp;</td><td align="right">  - </td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2da2013465%2ehlog?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test-a2013465.hlog</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">1</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2da2013465%2evswp?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test-a2013465.vswp</a></td><td align="right">14-Aug-2010 23:01</td><td align="right">402653184</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2dflat%2evmdk?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test-flat.vmdk</a></td><td align="right">26-Aug-2010 18:43</td><td align="right">17179869184</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2envram?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test.nvram</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">8684</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2evmdk?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test.vmdk</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">479</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2evmsd?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test.vmsd</a></td><td align="right">14-Aug-2010 16:59</td><td align="right">0</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2evmx?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test.vmx</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">3500</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/Fedora%2dclone%2dtest%2evmxf?dcPath=cupertino&amp;dsName=NFS%20datastore">Fedora-clone-test.vmxf</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">272</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/test%2etxt?dcPath=cupertino&amp;dsName=NFS%20datastore">test.txt</a></td><td align="right">24-Aug-2010 01:03</td><td align="right">12</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d2%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-2.log</a></td><td align="right">14-Aug-2010 16:51</td><td align="right">80782</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d3%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-3.log</a></td><td align="right">14-Aug-2010 19:07</td><td align="right">58573</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d4%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-4.log</a></td><td align="right">14-Aug-2010 23:00</td><td align="right">49751</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d5%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-5.log</a></td><td align="right">15-Aug-2010 00:04</td><td align="right">64024</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d6%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-6.log</a></td><td align="right">15-Aug-2010 00:11</td><td align="right">59742</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2d7%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware-7.log</a></td><td align="right">15-Aug-2010 00:13</td><td align="right">59859</td></tr>
		    <tr><td><a href="/folder/Fedora%2dclone%2dtest/vmware%2elog?dcPath=cupertino&amp;dsName=NFS%20datastore">vmware.log</a></td><td align="right">15-Aug-2010 00:23</td><td align="right">47157</td></tr>
		    <tr><th colspan="5"><hr></th></tr>
	    </table>
	      </body>
	    </html>
	*/    
	public String[] listDatastoreDirContent(String urlString) throws Exception {
		List<String> fileList = new ArrayList<String>();
		String content = new String(getResourceContent(urlString));
    	String marker = "</a></td><td ";
    	int parsePos = -1;
    	do {
    		parsePos = content.indexOf(marker, parsePos < 0 ? 0 : parsePos);
    		if(parsePos > 0) {
    			int beginPos = content.lastIndexOf('>', parsePos -1);
    			if(beginPos < 0)
    				beginPos = 0;
    			
    			fileList.add((content.substring(beginPos + 1, parsePos)));
    			parsePos += marker.length();
    		} else {
    			break;
    		}
    	} while(parsePos > 0);
		return fileList.toArray(new String[0]);
	}

	public String composeDatastoreBrowseUrl(String dcName, String fullPath) {
		DatastoreFile dsFile = new DatastoreFile(fullPath);
		return composeDatastoreBrowseUrl(dcName, dsFile.getDatastoreName(), dsFile.getRelativePath());
	}
	
	public String composeDatastoreBrowseUrl(String dcName, String datastoreName, String relativePath) {
		assert(relativePath != null);
		assert(datastoreName != null);
		
		StringBuffer sb = new StringBuffer();
		sb.append("https://");
		sb.append(_serverAddress);
		sb.append("/folder/");
		sb.append(relativePath);
		sb.append("?dcPath=").append(URLEncoder.encode(dcName)).append("&dsName=");
		sb.append(URLEncoder.encode(datastoreName));
		return sb.toString();
	}
	
	public HttpURLConnection getHTTPConnection(String urlString) throws Exception {
		return getHTTPConnection(urlString, "GET");
	}
	
	public HttpURLConnection getHTTPConnection(String urlString, String httpMethod) throws Exception {
		String cookieString = getServiceCookie();
	    HostnameVerifier hv = new HostnameVerifier() {
	    	public boolean verify(String urlHostName, SSLSession session) {
	    		return true;
	        }
	    };
	      
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);           
	    URL url = new URL(urlString);      
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	      
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setAllowUserInteraction(true);
	    conn.setRequestProperty(org.apache.axis.transport.http.HTTPConstants.HEADER_COOKIE, cookieString);
	    conn.setRequestMethod(httpMethod);
        connectWithRetry(conn);
	    return conn;
	}
	
	public HttpURLConnection getRawHTTPConnection(String urlString) throws Exception {
	    HostnameVerifier hv = new HostnameVerifier() {
	    	public boolean verify(String urlHostName, SSLSession session) {
	    		return true;
	        }
	    };
	      
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);           
	    URL url = new URL(urlString);      
	    return (HttpURLConnection)url.openConnection();
	}

	private String getServiceCookie() throws Exception {
		com.vmware.vim25.VimPortType service = getService();
		org.apache.axis.client.Stub st = (org.apache.axis.client.Stub)service;
		org.apache.axis.client.Call callObj = st._getCall();
		org.apache.axis.MessageContext msgContext = callObj.getMessageContext();
		String cookieString = (String)msgContext.getProperty(org.apache.axis.transport.http.HTTPConstants.HEADER_COOKIE);
		return cookieString;
	}
	
	private static void connectWithRetry(HttpURLConnection conn) throws Exception {
	    boolean connected = false;
	    for(int i = 0; i < MAX_CONNECT_RETRY && !connected; i++) {
	        try {
	            conn.connect();
	            connected = true;
                s_logger.info("Connected, conn: " + conn.toString() + ", retry: " + i);
	        } catch (Exception e) {
	            s_logger.warn("Unable to connect, conn: " + conn.toString() + ", message: " + e.toString() + ", retry: " + i);
	        
	            try {
	                Thread.sleep(CONNECT_RETRY_INTERVAL);
	            } catch(InterruptedException ex) {
	            }
	        }
	    }
	    
	    if(!connected)
	        throw new Exception("Unable to connect to " + conn.toString());
	}
	
	public void close() {
		try {
			_appUtil.disConnect();
		} catch(Exception e) {
			s_logger.warn("Unexpected exception: ", e);
		}
	}

	public static class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}
		
		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}
		
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
          	throws java.security.cert.CertificateException {
			return;
		}
		
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
          	throws java.security.cert.CertificateException {
			return;
		}
	}
}
