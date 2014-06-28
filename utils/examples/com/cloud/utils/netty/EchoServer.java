package com.cloud.utils.netty;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;

public class EchoServer {
	private static final Logger s_logger = Logger.getLogger(EchoServer.class);
	private NettyServer ns;

	public EchoServer() {
		ns = new NettyServer("NettyTestServer", 7777, 5, new TestServer());
	}

	public void start() {
		ns.start();
	}

	public void stop() {
		ns.stop();
	}

	private void doServerProcess(byte[] data, Link link) {
		link.send(ChannelBuffers.wrappedBuffer(data));
	}

	public void sendData(Link link) {
		byte[] b = new byte[1024];
		link.send(ChannelBuffers.wrappedBuffer(b));

	}

	public class TestServer implements HandlerFactory {

		@Override
		public Task create(Task.Type type, Link link, byte[] data) {
			return new NioTestServerHandler(type, link, data);
		}

		public class NioTestServerHandler extends Task {

			public NioTestServerHandler(Type type, Link link, byte[] data) {
				super(type, link, data);
			}

			// handler for several events
			@Override
			public void doTask(final Task task) {
				if (task.getType() == Task.Type.CONNECT) {

				} else if (task.getType() == Task.Type.DATA) {
					s_logger.debug("Server: Received DATA task");
					doServerProcess(task.getData(), task.getLink());
				} else if (task.getType() == Task.Type.DISCONNECT) {
					s_logger.debug("Server: Received DISCONNECT task");
				} else if (task.getType() == Task.Type.OTHER) {
					s_logger.debug("Server: Received OTHER task");
				}
			}

		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Server");
		EchoServer es = new EchoServer();
		try {
			es.start();
			System.out.println("Press any key to stop the server...");
			System.in.read();
		} finally {
			System.out.println("Stopping server...");
			es.stop();
			System.out.println("Stopped server...");
		}
	}
}