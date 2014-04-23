package cz.zcu.kiv.multicloud;

import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.CredentialStore;
import cz.zcu.kiv.multicloud.utils.AccountManager;

/**
 * cz.zcu.kiv.multicloud/MultiCloudSettings.java
 *
 * Class for accumulating custom settings for the MultiCloud library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloudSettings {

	/** Cloud settings manager. */
	private CloudManager cloudManager;
	/** Credential store. */
	private CredentialStore store;
	/** User account manager. */
	private AccountManager userManager;

	/**
	 * Ctor.
	 */
	public MultiCloudSettings() {
		cloudManager = null;
		store = null;
		userManager = null;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.CloudManager} used.
	 * @return Cloud settings manager.
	 */
	public CloudManager getCloudManager() {
		return cloudManager;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} used.
	 * @return Credential store.
	 */
	public CredentialStore getStore() {
		return store;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.AccountManager} used.
	 * @return User manager.
	 */
	public AccountManager getUserManager() {
		return userManager;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.CloudManager} to be used.
	 * @param cloudManager Cloud manager.
	 */
	public void setCloudManager(CloudManager cloudManager) {
		this.cloudManager = cloudManager;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} to be used.
	 * @param store Credential store.
	 */
	public void setStore(CredentialStore store) {
		this.store = store;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.AccountManager} to be used.
	 * @param userManager User manager.
	 */
	public void setUserManager(AccountManager userManager) {
		this.userManager = userManager;
	}

}
