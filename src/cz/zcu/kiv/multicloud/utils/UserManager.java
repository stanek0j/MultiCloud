package cz.zcu.kiv.multicloud.utils;

import java.util.Collection;

import cz.zcu.kiv.multicloud.json.UserSettings;

/**
 * cz.zcu.kiv.multicloud.core/UserManager.java
 *
 * Interface for storing user accounts for different cloud storage services.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface UserManager {

	/**
	 * Adds the {@link cz.zcu.kiv.multicloud.json.UserSettings} to local store.
	 * @param settings User settings.
	 */
	void addUserSettings(UserSettings settings);

	/**
	 * Returns all the {@link cz.zcu.kiv.multicloud.json.UserSettings} in the store.
	 * @return All user settings.
	 */
	Collection<UserSettings> getAllUserSettings();

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.json.UserSettings} specified by the identifier.
	 * @param userId User settings identifier.
	 * @return User settings.
	 */
	UserSettings getUserSettings(String userId);

	/**
	 * Removes the specified {@link cz.zcu.kiv.multicloud.json.UserSettings}.
	 * @param userId User settings identifier.
	 */
	void removeUserSettings(String userId);

}
