package cz.zcu.kiv.multicloud;

import java.io.IOException;

import cz.zcu.kiv.multicloud.json.UserSettings;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.CredentialStore;
import cz.zcu.kiv.multicloud.utils.FileCloudManager;
import cz.zcu.kiv.multicloud.utils.FileCredentialStore;
import cz.zcu.kiv.multicloud.utils.FileUserManager;
import cz.zcu.kiv.multicloud.utils.SecureFileCredentialStore;
import cz.zcu.kiv.multicloud.utils.UserManager;

/**
 * cz.zcu.kiv.multicloud/MultiCloud.java
 *
 * The MultiCloud library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloud {

	/** Cloud settings manager. */
	private CloudManager cloudManager;
	/** Credential store. */
	private CredentialStore store;
	/** User account manager. */
	private UserManager userManager;

	/**
	 * Empty ctor.
	 */
	public MultiCloud() {
		store = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		FileCloudManager cm = FileCloudManager.getInstance();
		try {
			cm.loadCloudSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cloudManager = cm;
		FileUserManager um = FileUserManager.getInstance();
		try {
			um.loadUserSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		userManager = um;
	}

	/**
	 * Ctor with custom settings supplied.
	 * @param settings Custom settings.
	 */
	public MultiCloud(MultiCloudSettings settings) {
		if (settings.getStore() == null) {
			store = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		} else {
			store = settings.getStore();
		}
		if (settings.getCloudManager() == null) {
			FileCloudManager cm = FileCloudManager.getInstance();
			try {
				cm.loadCloudSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cloudManager = cm;
		} else {
			cloudManager = settings.getCloudManager();
		}
		if (settings.getUserManager() == null) {
			FileUserManager um = FileUserManager.getInstance();
			try {
				um.loadUserSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			userManager = settings.getUserManager();
		}
	}

	/**
	 * Validates all user entries.
	 */
	public void validateUsers() {
		for (UserSettings user: userManager.getAllUserSettings()) {
			System.out.println("Validating user: " + user.getUserId());
			if (cloudManager.getCloudSettings(user.getSettingsId()) == null) {
				System.out.println("  Cloud settings \"" + user.getSettingsId() + "\" not found.");
			} else {
				System.out.println("  Cloud settings valid.");
			}
			OAuth2Token token;
			if ((token = store.retrieveCredential(user.getTokenId())) == null) {
				System.out.println("  Token \"" + user.getTokenId() + "\" not found.");
			} else {
				if (token.isExpired()) {
					System.out.println("  Token expired.");
				} else {
					System.out.println("  Token valid.");
				}
			}
		}
	}

}
