package com.cloud.utils.nio;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.cloud.utils.nio.Task.Type;

public class EchoClient implements Runnable {

	private static final Logger s_logger = Logger.getLogger(EchoClient.class);
	private static AtomicLong count = new AtomicLong(0);
	public static final long TOTAL_COUNT = 1000;

	private NioClient nc;
	private Link clientLink;
	private byte[] buffer = new byte[1024];
	private CountDownLatch latch = new CountDownLatch(1);
	private long start, end;// start and end time

	public EchoClient(String id) {
		nc = new NioClient("NioTestClient", "127.0.0.1", 7777, 5,
				new TestClient());
	}

	@Override
	public void run() {
		start = System.currentTimeMillis();
		nc.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			s_logger.warn("Interrupted Exception", e);
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
	private void doClientProcess(byte[] data) {
		long temp = count.incrementAndGet();
		if (temp > TOTAL_COUNT) {
			end = System.currentTimeMillis();
			shutdown();
		} else {
			if (temp > 0 && temp % (TOTAL_COUNT / 100) == 0)
				System.out.println("The total echo times are " + temp);
			try {
				clientLink.send(data);
			} catch (ClosedChannelException e) {
				s_logger.warn("The connection is closed", e);
				shutdown();
			}
		}

	}

	private void shutdown() {
		nc.stop();
		clientLink.close();
		latch.countDown();
	}

	public class TestClient implements HandlerFactory {

		@Override
		public Task create(Type type, Link link, byte[] data) {
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
					try {
						clientLink.send(buffer);
					} catch (ClosedChannelException e) {
						s_logger.warn("The connection is closed", e);
						shutdown();
					}
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
