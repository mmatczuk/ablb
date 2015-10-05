package com.github.mmatczuk.ablb.config;

/**
 * @author mmatczuk
 */
public class TestConfig extends Config {
	private final String[] mDistribution;

	public TestConfig(String[] distribution) {
		mDistribution = distribution;
	}

	@Override
	public String[] distribution() {
		return mDistribution;
	}
}
