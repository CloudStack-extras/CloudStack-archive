package com.cloud.bridge.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import com.cloud.bridge.service.core.s3.S3BucketAdapter;
import com.cloud.bridge.service.core.s3.S3MultipartPart;
import com.cloud.bridge.service.exception.InternalErrorException;
import com.cloud.bridge.service.exception.OutOfStorageException;
import com.cloud.bridge.service.exception.UnsupportedException;
import com.cloud.bridge.util.OrderedPair;

public class HdfsBucketAdapter implements S3BucketAdapter {
    protected final static Logger logger = Logger.getLogger(HdfsBucketAdapter.class);

    HdfsClient _client = new HdfsClient();

    public HdfsBucketAdapter() {
    }
    
    public void initialize() throws IOException {
        _client.initialize();
    }
    
    @Override
    public void createContainer(String mountedRoot, String bucket) {
        String dir = getBucketFolderDir(mountedRoot, bucket);
        try {
            _client.mkdir(dir);
        } catch (IOException e) {
            throw new OutOfStorageException("Unable to create container " + bucket, e);
        }

    }

    @Override
    public void deleteContainer(String mountedRoot, String bucket) {
        String dir = getBucketFolderDir(mountedRoot, bucket);
        try {
            _client.deleteFile(dir);
        } catch (IOException e) {
            throw new OutOfStorageException("Unable to delete " + dir + " for bucket " + bucket); 
        }

    }

    @Override
    public String getBucketFolderDir(String mountedRoot, String bucket) {
        String bucketFolder = getBucketFolderName(bucket);
        String dir;
        String separator = "" + File.separatorChar;
        if(!mountedRoot.endsWith(separator))
            dir = mountedRoot + separator + bucketFolder;
        else
            dir = mountedRoot + bucketFolder;
        
        return dir;
    }

    @Override
    public String saveObject(InputStream is, String mountedRoot, String bucket, String fileName) {
        String name = getBucketFolderDir(mountedRoot, bucket) + File.separatorChar + fileName;
        try {
            return _client.saveFile(is, name);
        } catch (IOException e) {
            throw new OutOfStorageException(e);
        }
    }

    @Override
    public DataHandler loadObject(String mountedRoot, String bucket, String fileName) {
        String src = getBucketFolderDir(mountedRoot, bucket) + File.separatorChar + fileName;
        try {
            return _client.readFile(src);
        } catch (IOException e) {
           throw new OutOfStorageException(e);
        }
    }

    @Override
    public DataHandler loadObjectRange(String mountedRoot, String bucket, String fileName, long startPos, long endPos) {
        String src = getBucketFolderDir(mountedRoot, bucket) + File.separatorChar + fileName;
        return _client.readRange(src, startPos, endPos);
    }

    @Override
    public void deleteObject(String mountedRoot, String bucket, String fileName) {
        String src = getBucketFolderDir(mountedRoot, bucket) + File.separatorChar + fileName;
        try {
            _client.deleteFile(src);
        } catch (IOException e) {
            throw new InternalErrorException("Unable to delete object " + src, e);
        }

    }

    @Override
    public OrderedPair<String, Long> concatentateObjects(String mountedRoot, String destBucket, String fileName, String sourceBucket, S3MultipartPart[] parts, OutputStream client) {
        throw new UnsupportedException("HDFS backing store does not support concatenate yet");
        //TODO
    }

    private String getBucketFolderName(String bucket) {
        // temporary 
        String name = bucket.replace(' ', '_');
        name = bucket.replace('\\', '-');
        name = bucket.replace('/', '-');
        
        return name;
    }
}
