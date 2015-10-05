package com.github.mmatczuk.ablb.dispather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.mmatczuk.ablb.config.TestConfig;
import com.github.mmatczuk.ablb.utils.TestHelper;

/**
 * @author mmatczuk
 */
public class DispatcherServiceTestCase {
	/**
	 * Number of groupName calls.
	 */
	public static final int DISTRIBUTION_TEST_ITERATIONS = 10000;

	/**
	 * Result tolerance.
	 */
	public static final float DISTRIBUTION_TEST_TOLERANCE = .05f;
	/**
	 * Number of groupName.
	 */
	public static final int IDEMPOTENCY_TEST_ITERATIONS = 10000;

	/**
	 * Check if distribution is preserved.
	 */
	@Test
	public void testDistribution() {
		final String[] distribution = new String[100];
		// group "0" - > 10
		for (int i = 0; i < 10; i++) {
			distribution[i] = "0";
		}
		// group "1" - > 20
		for (int i = 10; i < 30; i++) {
			distribution[i] = "1";
		}
		// group "2" - > 30
		for (int i = 30; i < 60; i++) {
			distribution[i] = "2";
		}
		// group "3" - > 40
		for (int i = 60; i < 100; i++) {
			distribution[i] = "3";
		}
		final DispatcherService service = getTestService(distribution);

		int[] result = {0, 0, 0, 0};
		for (int i = 0; i < DISTRIBUTION_TEST_ITERATIONS; i++) {
			final String groupName = service.groupName(TestHelper.randomString(5));
			result[Integer.valueOf(groupName)]++;
		}

		int expected;
		// "0"
		expected = DISTRIBUTION_TEST_ITERATIONS * 10 / 100;
		assertTrue(result[0] >= expected * (1 - DISTRIBUTION_TEST_TOLERANCE) && result[0] <= expected * (1 + DISTRIBUTION_TEST_TOLERANCE));
		// "1"
		expected = DISTRIBUTION_TEST_ITERATIONS * 20 / 100;
		assertTrue(result[1] >= expected * (1 - DISTRIBUTION_TEST_TOLERANCE) && result[1] <= expected * (1 + DISTRIBUTION_TEST_TOLERANCE));
		// "2"
		expected = DISTRIBUTION_TEST_ITERATIONS * 30 / 100;
		assertTrue(result[2] >= expected * (1 - DISTRIBUTION_TEST_TOLERANCE) && result[2] <= expected * (1 + DISTRIBUTION_TEST_TOLERANCE));
		// "3"
		expected = DISTRIBUTION_TEST_ITERATIONS * 40 / 100;
		assertTrue(result[3] >= expected * (1 - DISTRIBUTION_TEST_TOLERANCE) && result[3] <= expected * (1 + DISTRIBUTION_TEST_TOLERANCE));
	}

	/**
	 * Check if calling service multiple times gives the same result.
	 */
	@Test
	public void testIdempotency() {
		final String[] distribution = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		final DispatcherService service = getTestService(distribution);

		for (int i = 0; i < IDEMPOTENCY_TEST_ITERATIONS; i++) {
			final String userId = TestHelper.randomString(5);
			final String groupName = service.groupName(userId);
			for (int j = 0; j < 5; j++) {
				assertEquals(groupName, service.groupName(userId));
			}
		}
	}

	private DispatcherService getTestService(String[] distribution) {
		return new DispatcherServiceHashImpl(new TestConfig(distribution));
	}
}
