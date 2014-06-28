package com.cloud.utils.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

public class FrameEncoder implements ChannelDownstreamHandler {

	@Override
	public void handleDownstream(final ChannelHandlerContext context,
			final ChannelEvent evt) throws Exception {
		if (evt instanceof ChannelStateEvent) {
			ChannelStateEvent e = (ChannelStateEvent) evt;
			switch (e.getState()) {
			case OPEN:
			case CONNECTED:
			case BOUND:
				if (Boolean.FALSE.equals(e.getValue()) || e.getValue() == null) {
					context.sendDownstream(e);
					return;
				}
			}
		}
		if (!(evt instanceof MessageEvent)) {
			context.sendDownstream(evt);
			return;
		}

		MessageEvent e = (MessageEvent) evt;
		if (!(e.getMessage() instanceof ChannelBuffer)) {
			context.sendDownstream(evt);
			return;
		}
		ChannelBuffer msg = (ChannelBuffer) e.getMessage();
		int length = msg.readableBytes();

		ChannelBuffer frame = ChannelBuffers.dynamicBuffer(length + 4);
		frame.writeInt(length);
		frame.writeBytes(msg);

		context.sendDownstream(new DownstreamMessageEvent(evt.getChannel(), evt
				.getFuture(), frame, evt.getChannel().getRemoteAddress()));
	}
}
