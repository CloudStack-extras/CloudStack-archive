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

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import com.cloud.utils.concurrency.NamedThreadFactory;

public class NettyClient {
	private static final Logger s_logger = Logger.getLogger(NettyClient.class);;

	private ClientBootstrap _bootstrap;

	private ExecutorService _executor;

	private InetSocketAddress _address;

	private ChannelHandler _handler;

	private CountDownLatch handshakeLatch = new CountDownLatch(1);

	private ChannelGroup _channelGroup;

	private volatile boolean _isStartup;

	public NettyClient(final String name, String host, int port,
			final int workers, final HandlerFactory factory) {
		this(name, new InetSocketAddress(host, port), workers, factory);
	}

	public NettyClient(final String name, InetSocketAddress address,
			final int workers, final HandlerFactory factory) {
		_address = address;
		_bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		_executor = new ThreadPoolExecutor(workers, 5 * workers, 1,
				TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(),
				new NamedThreadFactory(name + "-Handler"));

		_channelGroup = new DefaultChannelGroup("netty-connector");
		_bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();

				SSLEngine engine = SslContextFactory.getClientContext()
						.createSSLEngine();
				engine.setUseClientMode(true);

				pipeline.addLast("framer-decoder", new FrameDecoder());
				pipeline.addLast("framer-encoder", new FrameEncoder());

				pipeline.addLast("ssl", new SslHandler(engine));

				// and then business logic.
				_handler = new ChannelHandler(factory, handshakeLatch,
						_executor, _channelGroup);
				pipeline.addLast("handler", _handler);

				return pipeline;
			}

		});
		_bootstrap.setOption("child.keepAlive", true);
		_bootstrap.setOption("tcpNoDelay", true);
	}

	public void start() {
		_bootstrap.connect(_address);
		try {
			handshakeLatch.await();
		} catch (InterruptedException e) {
			s_logger.warn("Interrupted start thread ", e);
		}
		_isStartup = _handler.isHandshakeCompleted();
	}

	public void stop() {
		_executor.shutdown();
		_channelGroup.close().awaitUninterruptibly();
		_bootstrap.releaseExternalResources();
		_isStartup = false;
	}

	public boolean isStartup() {
		return _isStartup;
	}

	public void schedule(Task task) {
		_handler.scheduleTask(task);
	}

	public void cleanUp() {
		_bootstrap.releaseExternalResources();
	}
}
