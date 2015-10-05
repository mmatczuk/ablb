/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mmatczuk.ablb.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

class OkHttpAsync implements HttpClient {
	private static final boolean VERBOSE = false;
	private final AtomicInteger requestsInFlight = new AtomicInteger();

	private OkHttpClient client;
	private Callback callback;
	private int concurrencyLevel;
	private int targetBacklog;

	private static long readAllAndClose(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		long total = 0;
		for (int count; (count = in.read(buffer)) != -1; ) {
			total += count;
		}
		in.close();
		return total;
	}

	@Override
	public void prepare(final Benchmark benchmark) {
		concurrencyLevel = benchmark.concurrencyLevel;
		targetBacklog = benchmark.targetBacklog;

		client = new OkHttpClient();
		client.setDispatcher(new Dispatcher(new ThreadPoolExecutor(benchmark.concurrencyLevel,
				benchmark.concurrencyLevel, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));

		callback = new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				System.out.println("Failed: " + e);
			}

			@Override
			public void onResponse(Response response) throws IOException {
				ResponseBody body = response.body();
				long total = readAllAndClose(body.byteStream());
				long finish = System.nanoTime();
				if (VERBOSE) {
					long start = (Long) response.request().tag();
					System.out.printf("Transferred % 8d bytes in %4d ms%n",
							total, TimeUnit.NANOSECONDS.toMillis(finish - start));
				}
				requestsInFlight.decrementAndGet();
			}
		};
	}

	@Override
	public void enqueue(HttpUrl url) throws Exception {
		requestsInFlight.incrementAndGet();
		client.newCall(new Request.Builder().tag(System.nanoTime()).url(url).build()).enqueue(callback);
	}

	@Override
	public synchronized boolean acceptingJobs() {
		return requestsInFlight.get() < (concurrencyLevel + targetBacklog);
	}
}
