package com.cloud.utils.netty;

public interface HandlerFactory {
	public Task create(Task.Type type, Link link, byte[] data);
}
