package com.cloud.utils.nio;

//calculate the throughput
public class ThrTestUtils {

	private ThrTestUtils() {

	}

	public static void displayAverage(final long numberOfMessages,
			final long start, final long end) {
		double duration = (1.0 * end - start) / 1000; // in seconds
		double average = 1.0 * numberOfMessages / duration;
		System.out.println(String.format(
				"average: %.2f msg/s (%d messages in %2.2fs)", average,
				numberOfMessages, duration));
	}

}
