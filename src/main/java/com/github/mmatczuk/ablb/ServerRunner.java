package com.github.mmatczuk.ablb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.github.mmatczuk.ablb.web.RouteHttpHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;

/**
 * Server factory.
 *
 * @author mmatczuk
 */
public abstract class ServerRunner {
	public static final String BASE_PACKAGE = "com.github.mmatczuk.ablb";

	/**
	 * Use proper factory method.
	 */
	private ServerRunner() {
	}

	/**
	 * @return server runner based on servletless undertow
	 */
	public static ServerRunner undertowRunner(int ioThreads, int workerThreads) {
		final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(PropertySourcesPlaceholderConfigurer.class);
		context.scan("com.github.mmatczuk.ablb");
		context.refresh();

		final Undertow.Builder builder = Undertow.builder()
				.addHttpListener(port(), host())
				.setServerOption(UndertowOptions.ENABLE_SPDY, true)
				.setDirectBuffers(true)
				.setHandler(Handlers
						.path()
						.addExactPath("/route", context.getBean(RouteHttpHandler.class)));

		if (ioThreads > 0) {
			builder.setIoThreads(ioThreads);
		}
		if (workerThreads > 0) {
			builder.setWorkerThreads(workerThreads);
		}

		return new UndertowServerRunner(builder.build());
	}

	/**
	 * Undertow server wrapper.
	 */
	static class UndertowServerRunner extends ServerRunner {
		final Undertow mServer;

		UndertowServerRunner(Undertow server) {
			mServer = server;
		}

		@Override
		public ServerRunner run() {
			mServer.start();
			return this;
		}

		@Override
		public ServerRunner terminate() {
			mServer.stop();
			return this;
		}
	}

	/**
	 * @return server runner based on spring boot
	 */
	public static ServerRunner bootRunner() {
		return new BootServerRunner();
	}

	/**
	 * Spring boot server wrapper.
	 */
	@SpringBootApplication
	@EnableAutoConfiguration
	@ComponentScan(BASE_PACKAGE)
	static class BootServerRunner extends ServerRunner {

		@Override
		public ServerRunner run() {
			System.setProperty("server.host", host());
			System.setProperty("server.port", String.valueOf(port()));
			SpringApplication.run(BootServerRunner.class);
			return this;
		}

		@Override
		public ServerRunner terminate() {
			return this;
		}
	}

	private static String host() {
		return System.getenv().getOrDefault("host", "0.0.0.0");
	}

	private static int port() {
		return Integer.parseInt(System.getenv().getOrDefault("port", "8080"));
	}

	/**
	 * Starts server.
	 */
	public abstract ServerRunner run();

	/**
	 * Stops server.
	 */
	public abstract ServerRunner terminate();
}
