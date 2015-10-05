package com.github.mmatczuk.ablb.utils;

import java.util.Random;

/**
 * @author mmatczuk
 */
public final class TestHelper {
	private static final Random sRandom = new Random(0);

	private TestHelper() {
		// helper
	}

	public static String randomString(int length) {
		String alphabet = "-abcdefghijklmnopqrstuvwxyz";
		char[] result = new char[length];
		for (int i = 0; i < length; i++) {
			result[i] = alphabet.charAt(sRandom.nextInt(alphabet.length()));
		}
		return new String(result);
	}
}
