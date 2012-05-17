package com.cloud.utils.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

public class Link {
	private Channel _channel;
	private Object _attach;

	public Link(Channel channel) {
		_channel = channel;
	}
	
	public ChannelFuture send(Object msg) {
		return _channel.write(msg);
	}
	
	public Channel getChannel() {
		return _channel;
	}

	public Object attachment() {
		return _attach;
	}

	public void attach(Object attach) {
		_attach = attach;
	}

	public ChannelFuture close() {
		return _channel.close();
	}

	public String getIpAddress() {
		// TODO Auto-generated method stub
		return null;
	}
}
