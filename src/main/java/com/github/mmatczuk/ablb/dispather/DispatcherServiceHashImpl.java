package com.github.mmatczuk.ablb.dispather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.mmatczuk.ablb.config.Config;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Hashing based dispatcher implementation. Requires a hash function and a probability distribution. Distribution is
 * provisioned as array of strings with desired string occurrence frequency.
 *
 * @author mmatczuk
 */
@Service
class DispatcherServiceHashImpl implements DispatcherService {
	private final HashFunction mHashFunction;
	private final String[] mDistribution;

	@Autowired
	DispatcherServiceHashImpl(Config config) {
		mHashFunction = Hashing.murmur3_128();
		mDistribution = config.distribution();
	}

	@Override
	public String groupName(String userId) {
		long hashValue = mHashFunction
				.hashString(userId, Charsets.UTF_8).asLong();
		if (hashValue == Long.MIN_VALUE) {
			hashValue += 1;
		}
		hashValue = Math.abs(hashValue) % mDistribution.length;

		return mDistribution[((int) hashValue)];
	}
}
