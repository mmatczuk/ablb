package com.github.mmatczuk.ablb.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * Reads configuration from yaml file.
 *
 * @author mmatczuk
 */
@Component
public class Config {
	/**
	 * Distribution size - 100%.
	 */
	public static final int DIST_SIZE = 100;

	private final File mConfigFile;
	private ConfigEntry[] mConfigEntries;

	@Autowired
	Config(@Value("${configFile:config.yml}") File configFile) {
		mConfigFile = configFile;
		init();
	}

	Config() {
		mConfigFile = null;
	}

	/**
	 * Loads config file, by default file config.yml is loaded.
	 */
	private void init() {
		try (InputStream in = new BufferedInputStream(new FileInputStream(mConfigFile))) {
			mConfigEntries = new Yaml().loadAs(in, ConfigEntry[].class);
		} catch (IOException e) {
			throw new RuntimeException("Configuration reading error", e);
		}
	}

	/**
	 * Creates group names distribution with one percent accuracy.
	 *
	 * @return string array of length 100
	 */
	public String[] distribution() {
		final String[] result = new String[DIST_SIZE];

		int weightSum = 0, i = 0;
		for (ConfigEntry ce : mConfigEntries) {
			checkArgument(ce.getWeight() > 0);
			checkArgument(ce.getWeight() <= DIST_SIZE - weightSum);
			weightSum += ce.getWeight();

			for (int j = 0; j < ce.getWeight(); j++) {
				result[i++] = ce.getName();
			}
		}

		checkArgument(weightSum == DIST_SIZE);

		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Config{");
		sb.append("mConfigFile=").append(mConfigFile);
		sb.append(", mConfigEntries=").append(Arrays.toString(mConfigEntries));
		sb.append('}');
		return sb.toString();
	}
}
