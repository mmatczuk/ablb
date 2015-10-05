package com.github.mmatczuk.ablb;

/**
 * Development server based on Undertow.
 *
 * @author mmatczuk
 */
public class MainUndertow {
	public static final int IO_THREADS = 8;
	public static final int WORKER_THREADS = 0;

	public static void main(final String[] args) {
		ServerRunner.undertowRunner(IO_THREADS, WORKER_THREADS).run();
	}
}
