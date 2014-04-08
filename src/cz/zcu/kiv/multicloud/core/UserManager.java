package cz.zcu.kiv.multicloud.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.core.json.Json;
import cz.zcu.kiv.multicloud.core.json.UserSettings;

/**
 * cz.zcu.kiv.multicloud.core/UserManager.java
 *
 * Class for managing user accounts for different cloud storage services.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class UserManager {

	/** Default file to save user settings. */
	public static final String DEFAULT_FILE = "user-settings.json";

	/** Instance of this class. */
	private static UserManager instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static UserManager getInstance() {
		if (instance == null) {
			instance = new UserManager();
		}
		return instance;
	}

	/** Map of all {@link cz.zcu.kiv.multicloud.core.json.UserSettings} loaded. */
	private final Map<String, UserSettings> users;
	/** Instance of the Jackson JSON components. */
	private final Json json;

	/**
	 * Private ctor.
	 */
	private UserManager() {
		users = new HashMap<>();
		json = Json.getInstance();
	}

	public void addUserSettings(UserSettings settings) {
		if (Utils.isNullOrEmpty(settings.getUserId())) {
			return;
		}
		users.put(settings.getUserId(), settings);
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.core.json.UserSettings} from the default file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings() throws IOException {
		loadUserSettings(new File(DEFAULT_FILE));
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.core.json.UserSettings} from specified file.
	 * @param file User settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings(File file) throws IOException {
		if (file.isFile()) {
			ObjectMapper om = json.getMapper();
			UserSettings us = om.readValue(file, UserSettings.class);
			if (Utils.isNullOrEmpty(us.getUserId())) {
				throw new JsonMappingException("User settings must contain \"user_id\" property.");
			}
			users.put(us.getUserId(), us);
		} else {
			throw new FileNotFoundException("Destination is not a file.");
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.core.json.UserSettings} from specified path.
	 * @param file Path to user settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings(String file) throws IOException {
		loadUserSettings(new File(file));
	}

	public void removeUserSettings(String userId) {
		users.remove(userId);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.core.json.UserSettings} to the default file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings() throws IOException {
		saveUserSettings(new File(DEFAULT_FILE));
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.core.json.UserSettings} to specified file.
	 * @param file User settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings(File file) throws IOException {
		ObjectMapper om = json.getMapper();
		om.writerWithDefaultPrettyPrinter().writeValue(file, users);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.core.json.UserSettings} to specified path.
	 * @param file Path to user settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings(String file) throws IOException {
		saveUserSettings(new File(file));
	}

}
