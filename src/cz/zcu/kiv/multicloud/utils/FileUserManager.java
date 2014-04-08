package cz.zcu.kiv.multicloud.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.json.UserSettings;

/**
 * cz.zcu.kiv.multicloud.utils/FileUserManager.java
 *
 * Class for managing user accounts for different cloud storage services. It uses files as a storage medium.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileUserManager implements UserManager {

	/** Default file to save user settings. */
	public static final String DEFAULT_FILE = "users.json";

	/** Instance of this class. */
	private static FileUserManager instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static FileUserManager getInstance() {
		if (instance == null) {
			instance = new FileUserManager();
		}
		return instance;
	}

	/** File to save the user settings to. */
	protected File settingsFile;

	/** Map of all {@link cz.zcu.kiv.multicloud.json.UserSettings} loaded. */
	private Map<String, UserSettings> users;
	/** Instance of the Jackson JSON components. */
	private final Json json;

	/**
	 * Private ctor.
	 */
	private FileUserManager() {
		users = new HashMap<>();
		json = Json.getInstance();
		settingsFile = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addUserSettings(UserSettings settings) {
		if (Utils.isNullOrEmpty(settings.getUserId())) {
			return;
		}
		users.put(settings.getUserId(), settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<UserSettings> getAllUserSettings() {
		return users.values();
	}

	/**
	 * Returns the {@link java.io.File} where the user settings is saved.
	 * @return User settings file.
	 */
	public File getSettingsFile() {
		return settingsFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserSettings getUserSettings(String userId) {
		return users.get(userId);
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.UserSettings} from the default file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings() throws IOException {
		if (settingsFile != null) {
			loadUserSettings(settingsFile);
		} else {
			loadUserSettings(new File(DEFAULT_FILE));
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.UserSettings} from specified file.
	 * @param file User settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings(File file) throws IOException {
		if (file.isFile()) {
			settingsFile = file;
			ObjectMapper om = json.getMapper();
			users = om.readValue(file, new TypeReference<HashMap<String, UserSettings>>() {});
		} else {
			throw new FileNotFoundException("Destination is not a file.");
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.UserSettings} from specified path.
	 * @param file Path to user settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadUserSettings(String file) throws IOException {
		loadUserSettings(new File(file));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeUserSettings(String userId) {
		users.remove(userId);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.UserSettings} to the default file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings() throws IOException {
		if (settingsFile != null) {
			saveUserSettings(settingsFile);
		} else {
			saveUserSettings(new File(DEFAULT_FILE));
		}
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.UserSettings} to specified file.
	 * @param file User settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings(File file) throws IOException {
		settingsFile = file;
		ObjectMapper om = json.getMapper();
		om.writerWithDefaultPrettyPrinter().writeValue(file, users);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.UserSettings} to specified path.
	 * @param file Path to user settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveUserSettings(String file) throws IOException {
		saveUserSettings(new File(file));
	}

	/**
	 * Sets the {@link java.io.File} where the user settings is saved.
	 * @param settingsFile User settings file.
	 */
	public void setSettingsFile(File settingsFile) {
		this.settingsFile = settingsFile;
	}

}
