package com.cloud.bridge.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.cloud.bridge.service.exception.FileNotExistException;
import com.cloud.bridge.service.exception.InternalErrorException;
import com.cloud.bridge.service.exception.OutOfStorageException;
import com.cloud.bridge.util.ConfigurationHelper;
import com.cloud.bridge.util.StringHelper;

public class HdfsClient {
    private static final class HdfsDataSource implements DataSource {
        InputStream istream;
        
        public HdfsDataSource(InputStream in) {
           istream = in;    
        }
        
        @Override
        public OutputStream getOutputStream() throws IOException {
            return null; //TODO
        }

        @Override
        public String getName() {
            return this.getClass().getName();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return istream;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }
    }
    protected final static Logger logger = Logger.getLogger(HdfsClient.class);

    Configuration conf = new Configuration();
    FileSystem fileSystem;
    boolean initialized = false;


    public HdfsClient() {

    }
    
    public void initialize() throws IOException {
        if (initialized) {
            return;
        }
        File coreCfg = ConfigurationHelper.findConfigurationFile("core-site.xml");
        File siteCfg = ConfigurationHelper.findConfigurationFile("hdfs-site.xml");
        conf.addResource(coreCfg.getAbsolutePath());
        conf.addResource(siteCfg.getAbsolutePath());
        fileSystem = FileSystem.get(conf);
        initialized = true;

    }

    public String saveFile(InputStream is, String dest) throws IOException {

        Path path = new Path(dest);
 
        FSDataOutputStream fos = null;
        MessageDigest md5 = null;
        
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalErrorException("Unable to get MD5 MessageDigest", e);
        }
        
        try {
            // -> when versioning is off we need to rewrite the file contents
            fileSystem.delete(path, true);
            fos = fileSystem.create(path);
            
            byte[] buffer = new byte[4096];
            int len = 0;
            while( (len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                md5.update(buffer, 0, len);
                
            }       
            //Convert MD5 digest to (lowercase) hex String
            return StringHelper.toHexString(md5.digest());
            
        } 
        catch(IOException e) {
            throw new OutOfStorageException(e);
        }
        finally {
            try {
                if (null != fos) fos.close();
            }
            catch( Exception e ) {
                logger.error("Can't close FileOutputStream " + e.getMessage(), e);          
            }
        }
        
    }

    public DataHandler readFile(String file) throws IOException {

        Path path = new Path(file);
        if (!fileSystem.exists(path)) {
            throw new FileNotExistException("Unable to open underlying object file");
        }

        FSDataInputStream in = fileSystem.open(path);

        return new DataHandler(new HdfsDataSource(in));
    }
    
    public DataHandler readRange(String file, long startPos, long endPos) {
        try {
            DataSource ds = new HdfsRangeDataSource(fileSystem, file, startPos, endPos);
            return new DataHandler(ds);
        } catch(IOException e) {
            throw new FileNotExistException("Unable to open underlying object file");
        }
    }

    public void deleteFile(String file) throws IOException {

        Path path = new Path(file);
        if (!fileSystem.exists(path)) {
            throw new FileNotExistException("File " + file + " does not exist");
        }
        fileSystem.delete(path, true);

    }

    public void mkdir(String dir) throws IOException {

        Path path = new Path(dir);
        if (fileSystem.exists(path)) {
            return;
        }

        fileSystem.mkdirs(path);

    }
    
    public void createFile(String fileName) throws IOException {

        Path path = new Path(fileName);
        if (fileSystem.exists(path)) {
            return;
        }

        fileSystem.create(path);

    }


}
