package com.github.mmatczuk.ablb.dispather;

/**
 * Assigns user to group.
 *
 * @author mmatczuk
 */
public interface DispatcherService {
	/**
	 * @return group name for given user id
	 */
	String groupName(String userId);
}
