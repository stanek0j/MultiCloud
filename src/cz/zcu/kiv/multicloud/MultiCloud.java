package cz.zcu.kiv.multicloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.OAuth2;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Error;
import cz.zcu.kiv.multicloud.oauth2.OAuth2ErrorType;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.CredentialStore;
import cz.zcu.kiv.multicloud.utils.FileAccountManager;
import cz.zcu.kiv.multicloud.utils.FileCloudManager;
import cz.zcu.kiv.multicloud.utils.FileCredentialStore;
import cz.zcu.kiv.multicloud.utils.SecureFileCredentialStore;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud/MultiCloud.java
 *
 * The MultiCloud library.
 * All methods are synchronous. For asynchronous usage, external threading should be used.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloud {

	/** Cloud settings manager. */
	private CloudManager cloudManager;
	/** Credential store. */
	private CredentialStore credentialStore;
	/** User account manager. */
	private AccountManager accountManager;

	/**
	 * Empty ctor.
	 */
	public MultiCloud() {
		credentialStore = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		FileCloudManager cm = FileCloudManager.getInstance();
		try {
			cm.loadCloudSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cloudManager = cm;
		FileAccountManager um = FileAccountManager.getInstance();
		try {
			um.loadAccountSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		accountManager = um;
	}

	/**
	 * Ctor with custom settings supplied.
	 * @param settings Custom settings.
	 */
	public MultiCloud(MultiCloudSettings settings) {
		if (settings.getCredentialStore() == null) {
			credentialStore = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		} else {
			credentialStore = settings.getCredentialStore();
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
		if (settings.getAccountManager() == null) {
			FileAccountManager um = FileAccountManager.getInstance();
			try {
				um.loadAccountSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			accountManager = settings.getAccountManager();
		}
	}

	/**
	 * Runs the authorization process for the specified user account.
	 * For certain authorization flows, this operation is blocking.
	 * @param name Name of the user account.
	 * @param callback Callback from the authorization process, if necessary.
	 * @throws MultiCloudException If some parameters were wrong.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the authorization process was interrupted.
	 */
	public void authorizeAccount(String name, AuthorizationCallback callback) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		AccountSettings account = accountManager.getAccountSettings(name);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (account.isAuthorized()) {
			throw new MultiCloudException("User account already authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2 auth = new OAuth2(Utils.cloudSettingsToOAuth2Settings(settings), credentialStore);
		if (callback != null) {
			auth.setAuthCallback(callback);
		}
		OAuth2Error error = auth.authorize(null);
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			throw new MultiCloudException("Authorization failed.");
		} else {
			if (!Utils.isNullOrEmpty(account.getTokenId())) {
				credentialStore.deleteCredential(account.getTokenId());
			}
			account.setTokenId(auth.getObtainedStoreKey());
		}
		accountManager.saveAccountSettings();
	}

	/**
	 * Creates new user account and stores it in the account manager.
	 * @param name Name for the user account.
	 * @param cloudStorage Name of the cloud storage service provider.
	 * @throws MultiCloudException If the given parameters were wrong.
	 */
	public void createAccount(String name, String cloudStorage) throws MultiCloudException {
		if (accountManager.getAccountSettings(name) != null) {
			throw new MultiCloudException("User account already exists.");
		}
		if (cloudManager.getCloudSettings(cloudStorage) == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		AccountSettings account = new AccountSettings();
		account.setUserId(name);
		account.setSettingsId(cloudStorage);
		accountManager.addAccountSettings(account);
	}

	/**
	 * Delete previously created user account, including associated token in the store.
	 * @param name Name of the user account.
	 * @throws MultiCloudException If the given parameter was wrong.
	 */
	public void deleteAccount(String name) throws MultiCloudException {
		AccountSettings account = accountManager.getAccountSettings(name);
		if (account == null) {
			throw new MultiCloudException("User account does not exist.");
		}
		if (account.isAuthorized()) {
			credentialStore.deleteCredential(account.getTokenId());
		}
		accountManager.removeAccountSettings(name);
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.AccountManager}, {@link cz.zcu.kiv.multicloud.utils.CloudManager} and {@link cz.zcu.kiv.multicloud.utils.CredentialStore} used in this library instance.
	 * @return Settings used in the instance of the library.
	 */
	public MultiCloudSettings getSettings() {
		MultiCloudSettings settings = new MultiCloudSettings();
		settings.setAccountManager(accountManager);
		settings.setCloudManager(cloudManager);
		settings.setCredentialStore(credentialStore);
		return settings;
	}

	/**
	 * Runs the process for access token refreshing.
	 * Should be used only if the {@link cz.zcu.kiv.multicloud.oauth2.OAuth2Token} contains refresh token, otherwise it fails.
	 * @param name Name of the user account.
	 * @param callback Callback from the authorization process, if necessary.
	 * @throws MultiCloudException If some parameters were wrong.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the authorization process was interrupted.
	 */
	public void refreshAccount(String name, AuthorizationCallback callback) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		AccountSettings account = accountManager.getAccountSettings(name);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2 auth = new OAuth2(Utils.cloudSettingsToOAuth2Settings(settings), credentialStore);
		if (callback != null) {
			auth.setAuthCallback(callback);
		}
		OAuth2Error error = auth.refresh(account.getTokenId());
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			throw new MultiCloudException("Refreshing token failed.");
		}
		accountManager.saveAccountSettings();
	}

	/**
	 * Validates all user account entries.
	 */
	public void validateAccounts() {
		List<String> usedTokens = new ArrayList<>();
		/* remove broken token links */
		for (AccountSettings account: accountManager.getAllAccountSettings()) {
			if (credentialStore.retrieveCredential(account.getTokenId()) == null) {
				account.setTokenId(null);
			} else {
				usedTokens.add(account.getTokenId());
			}
		}
		accountManager.saveAccountSettings();
		/* remove unused tokens */
		for (String token: credentialStore.getIdentifiers()) {
			if (!usedTokens.contains(token)) {
				credentialStore.deleteCredential(token);
			}
		}
	}

}
