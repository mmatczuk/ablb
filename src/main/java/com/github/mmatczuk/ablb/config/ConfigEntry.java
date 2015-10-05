package com.github.mmatczuk.ablb.config;

/**
 * Config object created by yaml parser.
 *
 * @author mmatczuk
 */
public class ConfigEntry {
	private String mName;
	private int mWeight;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public int getWeight() {
		return mWeight;
	}

	public void setWeight(int weight) {
		mWeight = weight;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ConfigEntry{");
		sb.append("mName='").append(mName).append('\'');
		sb.append(", mWeight=").append(mWeight);
		sb.append('}');
		return sb.toString();
	}
}
