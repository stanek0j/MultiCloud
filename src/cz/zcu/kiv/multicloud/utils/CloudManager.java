package cz.zcu.kiv.multicloud.utils;

import cz.zcu.kiv.multicloud.json.CloudSettings;

/**
 * cz.zcu.kiv.multicloud.utils/CloudManager.java			<br /><br />
 *
 * Interface for loading settings for different cloud storage service providers.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface CloudManager {

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.json.CloudSettings} identified by its name.
	 * @param cloudName Name of the settings.
	 * @return Cloud settings.
	 */
	CloudSettings getCloudSettings(String cloudName);

}
