package com.github.mmatczuk.ablb.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.mmatczuk.ablb.ServerRunner;
import com.github.mmatczuk.ablb.utils.TestHelper;
import com.google.caliper.Param;
import com.google.caliper.api.VmOptions;
import com.google.caliper.model.ArbitraryMeasurement;
import com.google.caliper.runner.CaliperMain;
import com.squareup.okhttp.HttpUrl;

/**
 * @author mmatczuk
 */
@VmOptions("-XX:-TieredCompilation")
public class Benchmark {
	private static final int NUM_REPORTS = 25;
	private static final boolean VERBOSE = true;

	/**
	 * How many concurrent requests to execute.
	 */
	@Param({"40"})
	int concurrencyLevel;

	/**
	 * How many requests to enqueue to await threads to execute them.
	 */
	@Param({"10"})
	int targetBacklog;

	/**
	 * Undertow ioThreads, 0 - use defaults.
	 */
	@Param({"8", "16"})
	int ioThreads;

	/**
	 * Undertow workerThreads, 0 - use defaults.
	 */
	@Param({"0"})
	int workerThreads;

	public static void main(String[] args) {
		List<String> allArgs = new ArrayList<>();
		allArgs.add("--instrument");
		allArgs.add("arbitrary");
		allArgs.addAll(Arrays.asList(args));

		CaliperMain.main(Benchmark.class, allArgs.toArray(new String[allArgs.size()]));
	}

	@ArbitraryMeasurement(description = "requests per second")
	public double run() throws Exception {
		if (VERBOSE) System.out.println(toString());

		final HttpClient httpClient = getHttpClient();
		final ServerRunner serverRunner = getServerRunner();

		int requestCount = 0;
		long reportStart = System.nanoTime();
		long reportPeriod = TimeUnit.SECONDS.toNanos(1);
		int reports = 0;
		double best = 0.0;

		try {
			// Run until we've printed enough reports.
			while (reports < NUM_REPORTS) {
				// Print a report if we haven't recently.
				long now = System.nanoTime();
				double reportDuration = now - reportStart;
				if (reportDuration > reportPeriod) {
					double requestsPerSecond = requestCount / reportDuration * TimeUnit.SECONDS.toNanos(1);
					if (VERBOSE) {
						System.out.println(String.format("Requests per second: %.1f", requestsPerSecond));
					}
					best = Math.max(best, requestsPerSecond);
					requestCount = 0;
					reportStart = now;
					reports++;
				}

				// Fill the job queue with work.
				while (httpClient.acceptingJobs()) {
					httpClient.enqueue(newUrl());
					requestCount++;
				}

				// The job queue is full. Take a break.
				sleep(1);
			}
		} finally {
			serverRunner.terminate();
		}

		return best;
	}

	private ServerRunner getServerRunner() {
		final ServerRunner serverRunner = ServerRunner.undertowRunner(ioThreads, workerThreads);
		serverRunner.run();
		return serverRunner;
	}

	private HttpClient getHttpClient() {
		HttpClient httpClient = new OkHttpAsync();
		httpClient.prepare(this);
		return httpClient;
	}

	private HttpUrl newUrl() {
		return HttpUrl.parse("http://localhost:8080/route?id=" + TestHelper.randomString(10));
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Benchmark{");
		sb.append("concurrencyLevel=").append(concurrencyLevel);
		sb.append(", targetBacklog=").append(targetBacklog);
		sb.append(", ioThreads=").append(ioThreads);
		sb.append(", workerThreads=").append(workerThreads);
		sb.append('}');
		return sb.toString();
	}
}
