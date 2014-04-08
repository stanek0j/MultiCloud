package cz.zcu.kiv.multicloud.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.core.json.CloudSettings;
import cz.zcu.kiv.multicloud.core.json.Json;

/**
 * cz.zcu.kiv.multicloud.core/CloudManager.java
 *
 * Class for managing settings for different cloud storage service providers.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudManager {

	/** Default location of cloud storage settings files. */
	public static final String DEFAULT_LOCATION = "definitions";
	/** Default file suffix for loading settings. */
	public static final String DEFAULT_FILE_SUFFIX = ".json";

	/** Instance of this class. */
	private static CloudManager instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static CloudManager getInstance() {
		if (instance == null) {
			instance = new CloudManager();
		}
		return instance;
	}

	/** Map of all {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} loaded. */
	private final Map<String, CloudSettings> settings;
	/** Instance of the Jackson JSON components. */
	private final Json json;

	/**
	 * Private ctor.
	 */
	private CloudManager() {
		settings = new HashMap<>();
		json = Json.getInstance();
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} identified by its name.
	 * @param cloudName Name of the settings.
	 * @return Cloud settings.
	 */
	public CloudSettings getCloudSettings(String cloudName) {
		return settings.get(cloudName);
	}

	/**
	 * Load {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} from default location.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings() throws IOException {
		loadCloudSettings(new File(DEFAULT_LOCATION));
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} from specified path.
	 * If the path parameter points to a folder, all the files in the folder are loaded.
	 * @param path Path to be loaded.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings(File path) throws IOException {
		if (path.isDirectory()) {
			loadFolder(path);
		} else if (path.isFile()) {
			loadFile(path);
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} from specified path.
	 * If the path parameter points to a folder, all the files in the folder are loaded.
	 * @param path Path to be loaded.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings(String path) throws IOException {
		loadCloudSettings(new File(path));
	}

	/**
	 * Loads a single {@link cz.zcu.kiv.multicloud.core.json.CloudSettings} from file.
	 * @param path Path to the file.
	 * @throws IOException If the file cannot be loaded.
	 */
	private void loadFile(File path) throws IOException {
		ObjectMapper om = json.getMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		CloudSettings cs = om.readValue(path, CloudSettings.class);
		if (Utils.isNullOrEmpty(cs.getSettingsId())) {
			throw new JsonMappingException("File must contain \"name\" property.");
		}
		if (!settings.containsKey(cs.getSettingsId())) {
			settings.put(cs.getSettingsId(), cs);
		}
	}

	/**
	 * Loads all files in the supplied folder.
	 * @param path Path to the folder.
	 * @throws IOException If one or more of the files cannot be loaded.
	 */
	private void loadFolder(File path) throws IOException {
		/* filter files in the folder */
		FilenameFilter filter = new FilenameFilter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(DEFAULT_FILE_SUFFIX);
			}
		};
		/* try to load all the files in the folder */
		List<String> failed = new ArrayList<>();
		for (File f: path.listFiles(filter)) {
			try {
				loadFile(f);
			} catch (IOException e) {
				failed.add(f.getName());
			}
		}
		/* throw an exception if some of the files cannot be loaded */
		if (!failed.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed to load one or more files:");
			for (int i = 0; i < failed.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(" " + failed.get(i));
			}
			throw new IOException(sb.toString());
		}
	}

}
