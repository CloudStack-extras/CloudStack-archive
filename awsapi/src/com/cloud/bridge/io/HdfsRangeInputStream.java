/*
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.bridge.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * @author Kelven Yang
 */
public class HdfsRangeInputStream extends InputStream {
	private FSDataInputStream in;
	private long curPos;
	private long endPos; 
	private long fileLength;
	
	public HdfsRangeInputStream(FileSystem hdfs, Path src, long startPos, long endPos) throws IOException {
		FileStatus status = hdfs.getFileStatus(src);
		fileLength = status.getLen();
		
		if(startPos > fileLength)
			startPos = fileLength;
		
		if(endPos > fileLength)
			endPos = fileLength;
		
		if(startPos > endPos)
			throw new IllegalArgumentException("Invalid file range " + startPos + "-" + endPos);

		this.curPos = startPos;
		this.endPos = endPos;
		in = hdfs.open(src);

		in.seek(startPos);
	}
	
	@Override
	public int available() throws IOException {
		return (int)(endPos - curPos);
	}

	@Override
	public int read() throws IOException {
		if(available() > 0) {
			int value = in.read();
			curPos++;
			return value;
		}
		return -1;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesToRead = Math.min(len, available());
		if(bytesToRead == 0)
			return -1;
		
		int bytesRead = in.read(b, off, bytesToRead);
		if(bytesRead < 0)
			return -1;
		
		curPos += bytesRead;
		return bytesRead;
	}
	
	@Override
	public long skip(long n) throws IOException {
		long skipped = Math.min(n, available());
		in.skipBytes((int)skipped);
		curPos += skipped;
		return skipped;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
}
