package com.cloud.utils.nio;

import java.nio.channels.ClosedChannelException;
import org.apache.log4j.Logger;
import com.cloud.utils.nio.Task.Type;

public class EchoServer {
	private static final Logger s_logger = Logger.getLogger(EchoServer.class);
	private NioServer ns;

	public EchoServer() {
		ns = new NioServer("NioTestServer", 7777, 5, new TestServer());
	}

	public void start() {
		ns.start();
	}

	public void stop() {
		ns.stop();
	}

	private void doServerProcess(byte[] data, Link link) {
		try {
			link.send(data);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendData(Link link) {
		byte[] b = new byte[1024];
		try {
			link.send(b);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class TestServer implements HandlerFactory {

		@Override
		public Task create(Type type, Link link, byte[] data) {
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
					task.getLink().close();
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