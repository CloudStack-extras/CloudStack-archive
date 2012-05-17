package com.cloud.utils.netty;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import com.cloud.utils.concurrency.NamedThreadFactory;

public class NettyServer {
    private ServerBootstrap _bootstrap;
	
	private ExecutorService _executor;

	private int _port;
	
	private ChannelHandler _handler;
	
	private CountDownLatch handshakeLatch = new CountDownLatch(1);

	private ChannelGroup _channelGroup;

	private boolean _isRunning = false;
		
	public NettyServer(final String name, int port, final int workers, final HandlerFactory factory) {
		this._port = port;
		_bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		
		_executor = new ThreadPoolExecutor(workers, 5 * workers, 1,
				TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(),
				new NamedThreadFactory(name + "-Handler"));
		
	    _channelGroup = new DefaultChannelGroup("netty-acceptor");
		_bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();

				SSLEngine engine = SslContextFactory.getServerContext()
						.createSSLEngine();
				engine.setUseClientMode(false);

				pipeline.addLast("framer-decoder", new FrameDecoder());
				pipeline.addLast("framer-encoder", new FrameEncoder());

				pipeline.addLast("ssl", new SslHandler(engine));

				// and then business logic.
				_handler = new ChannelHandler(factory, handshakeLatch, _executor, _channelGroup);
				pipeline.addLast("handler", _handler);

				return pipeline;
			}
			
		});
		_bootstrap.setOption("child.keepAlive", true);
		_bootstrap.setOption("reuseAddress", true);
		_bootstrap.setOption("child.reuseAddress", true);
		_bootstrap.setOption("tcpNoDelay", true);
		_bootstrap.setOption("backlog", 8192); 		
	}
	
	public void start() {
		_bootstrap.bind(new InetSocketAddress(_port));
		_isRunning  = true;
	}
	
	public void stop() {
		_executor.shutdown();
	    _channelGroup.close().awaitUninterruptibly();
		_bootstrap.releaseExternalResources();
		_isRunning = false;
	}

	public boolean isRunning() {
		return _isRunning;
	}

}
