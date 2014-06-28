package com.cloud.utils.netty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;

public class ChannelHandler extends SimpleChannelUpstreamHandler {
	private static final Logger s_logger = Logger
			.getLogger(ChannelHandler.class);

	private HandlerFactory _factory;

	private ExecutorService _executor;

	private boolean _handshakeCompleted = false;

	private CountDownLatch _handshakeLatch;

	private ChannelGroup _channelGroup;

	public ChannelHandler(HandlerFactory factory,
			CountDownLatch handshakeLatch, ExecutorService executor,
			ChannelGroup channelGroup) {
		_factory = factory;
		_handshakeLatch = handshakeLatch;
		_executor = executor;
		_channelGroup = channelGroup;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		_channelGroup.add(e.getChannel());
		ctx.sendUpstream(e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

		// Begin handshake.
		ChannelFuture handshakeFuture = sslHandler.handshake();
		handshakeFuture.addListener(new HandshakeListener());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
		Task task = _factory.create(Task.Type.DATA, new Link(ctx.getChannel()),
				buffer.array());
		_executor.execute(task);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		s_logger.warn("Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Task task = _factory.create(Task.Type.DISCONNECT,
				new Link(ctx.getChannel()), null);
		_executor.execute(task);
	}

	private class HandshakeListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				Task task = _factory.create(Task.Type.CONNECT,
						new Link(future.getChannel()), null);
				_executor.execute(task);
				_handshakeCompleted = true;
				_handshakeLatch.countDown();
			} else {
				future.getChannel().close();
			}
		}
	}

	public boolean isHandshakeCompleted() {
		return _handshakeCompleted;
	}

	public void scheduleTask(Task task) {
		_executor.execute(task);
	}

}
