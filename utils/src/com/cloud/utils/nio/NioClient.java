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
package com.cloud.utils.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

public class NioClient extends NioConnection {
    private static final Logger s_logger = Logger.getLogger(NioClient.class);
    
    protected String _host;
    protected String _bindAddress;
    
    public NioClient(String name, String host, int port, int workers, HandlerFactory factory) {
        super(name, port, workers, factory);
        _host = host;
    }
    
    public void setBindAddress(String ipAddress) {
    	_bindAddress = ipAddress;
    }
    
    @Override
    protected void init() throws IOException {
        _selector = Selector.open();
        
        SocketChannel sch = SocketChannel.open();
        sch.configureBlocking(true);
        s_logger.info("Connecting to " + _host + ":" + _port);

        if(_bindAddress != null) {
            s_logger.info("Binding outbound interface at " + _bindAddress);
            
            InetSocketAddress addr = new InetSocketAddress(_bindAddress, 0);
            sch.socket().bind(addr);
        }

        InetSocketAddress addr = new InetSocketAddress(_host, _port);
        sch.connect(addr);

        SSLEngine sslEngine = null;
        try {
        	// Begin SSL handshake in BLOCKING mode
        	sch.configureBlocking(true);

        	SSLContext sslContext = initSSLContext(true);
        	sslEngine = sslContext.createSSLEngine(_host, _port);
        	sslEngine.setUseClientMode(true);

        	doHandshake(sch, sslEngine, true);
        	s_logger.info("SSL: Handshake done");
        } catch (Exception e) {
        	throw new IOException("SSL: Fail to init SSL! " + e);
        }
        
        sch.configureBlocking(false);
        Link link = new Link(addr, this);
        link.setSSLEngine(sslEngine);
        SelectionKey key = sch.register(_selector, SelectionKey.OP_READ);
        link.setKey(key);
        key.attach(link);
        // Notice we've already connected due to the handshake, so let's get the
        // remaining task done
        Task task = _factory.create(Task.Type.CONNECT, link, null);
        _executor.execute(task);
    }
    
    @Override
    protected void registerLink(InetSocketAddress saddr, Link link) {
        // don't do anything.
    }
    
    @Override
    protected void unregisterLink(InetSocketAddress saddr) {
        // don't do anything.
    }
}
