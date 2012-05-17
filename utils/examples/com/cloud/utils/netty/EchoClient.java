package com.cloud.utils.netty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import org.jboss.netty.buffer.ChannelBuffers;

public class EchoClient implements Runnable {
	private static final Logger s_logger = Logger.getLogger(EchoClient.class);
	private static AtomicLong count = new AtomicLong(0);
	public static final long TOTAL_COUNT = 1000;

	private NettyClient nc;
	private Link clientLink;
	private byte[] buffer = new byte[1024];
	private CountDownLatch latch = new CountDownLatch(1);

	private long start, end;// start and end time

	public EchoClient() {
		nc = new NettyClient("NettyTestClient", "127.0.0.1", 7777, 5,
				new TestClient());
	}

	@Override
	public void run() {
		start = System.currentTimeMillis();
		nc.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void setClientLink(Link link) {
		clientLink = link;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	// process the data received from the server
	protected void doClientProcess(byte[] data) {
		long temp = count.incrementAndGet();
		if (temp > TOTAL_COUNT) {
			end = System.currentTimeMillis();
			// shutdown
			closeLink();
			stopClient();
			latch.countDown();
		} else {
			if (temp > 0 && temp % (TOTAL_COUNT / 100) == 0)
				System.out.println("The total echo times are " + temp);
			clientLink.send(ChannelBuffers.wrappedBuffer(data));
		}

	}

	public void stopClient() {
		nc.stop();
	}

	public void closeLink() {
		clientLink.close();
	}

	public class TestClient implements HandlerFactory {

		@Override
		public Task create(Task.Type type, Link link, byte[] data) {
			return new NioTestServerHandler(type, link, data);
		}

		public class NioTestServerHandler extends Task {

			public NioTestServerHandler(Type type, Link link, byte[] data) {
				super(type, link, data);
			}

			// the handler for several events
			@Override
			public void doTask(final Task task) {
				if (task.getType() == Task.Type.CONNECT) {
					s_logger.debug("Client: Received CONNECT task");
					setClientLink(task.getLink());
					clientLink.send(ChannelBuffers.wrappedBuffer(buffer));
				} else if (task.getType() == Task.Type.DATA) {
					s_logger.debug("Client: Received DATA task");
					doClientProcess(task.getData());
				} else if (task.getType() == Task.Type.DISCONNECT) {
					s_logger.debug("Client: Received DISCONNECT task");
				} else if (task.getType() == Task.Type.OTHER) {
					s_logger.debug("Client: Received OTHER task");
				}
			}

		}
	}
}
