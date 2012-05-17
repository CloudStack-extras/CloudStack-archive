package com.cloud.utils.nio;

public class Client {

	private long start = Long.MAX_VALUE, end = 0;

	public void CalcStartAndEnd(EchoClient ec[]) {
		for (int i = 0; i < ec.length; ++i) {
			if (ec[i].getStart() < start) {
				start = ec[i].getStart();
			}
			if (ec[i].getEnd() > end) {
				end = ec[i].getEnd();
			}
		}

	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public static void main(String args[]) {

		int num = 1;// number of threads
		Client c = new Client();

		Thread threads[] = new Thread[num];
		EchoClient ec[] = new EchoClient[num];

		for (int i = 0; i < num; ++i) {
			ec[i] = new EchoClient("clientThread" + i);
			threads[i] = new Thread(ec[i]);
			threads[i].start();
		}

		for (int i = 0; i < num; ++i) {
			try {
				// wait for the thread to finish
				threads[i].join();
			} catch (InterruptedException e) {
				// if the join interrupts the thread, print an error
				e.printStackTrace();
			}
		}

		c.CalcStartAndEnd(ec);
		ThrTestUtils.displayAverage(EchoClient.TOTAL_COUNT, c.getStart(),
				c.getEnd());// throughput

		System.out.println("The duration is " + (c.getEnd() - c.getStart())
				* 1.0 / 1000 + " seconds");

	}
}
