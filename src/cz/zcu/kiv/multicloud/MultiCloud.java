package cz.zcu.kiv.multicloud;

import java.io.IOException;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.OAuth2;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Error;
import cz.zcu.kiv.multicloud.oauth2.OAuth2ErrorType;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
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
	private CredentialStore store;
	/** User account manager. */
	private AccountManager accountManager;

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
			FileAccountManager um = FileAccountManager.getInstance();
			try {
				um.loadAccountSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			accountManager = settings.getUserManager();
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
			throw new MultiCloudException("User not found.");
		}
		if (account.isAuthorized()) {
			throw new MultiCloudException("User already authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2 auth = new OAuth2(Utils.cloudSettingsToOAuth2Settings(settings), store);
		if (callback != null) {
			auth.setAuthCallback(callback);
		}
		OAuth2Error error = auth.authorize(null);
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			throw new MultiCloudException("Authorization failed.");
		} else {
			account.setTokenId(auth.getObtainedStoreKey());
		}
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
			store.deleteCredential(account.getTokenId());
		}
		accountManager.removeAccountSettings(name);
	}

	/**
	 * Validates all user entries.
	 */
	public void validateAccount() {
		for (AccountSettings account: accountManager.getAllAccountSettings()) {
			System.out.println("Validating account: " + account.getAccountId());
			if (cloudManager.getCloudSettings(account.getSettingsId()) == null) {
				System.out.println("  Cloud settings \"" + account.getSettingsId() + "\" not found.");
			} else {
				System.out.println("  Cloud settings valid.");
			}
			OAuth2Token token;
			if ((token = store.retrieveCredential(account.getTokenId())) == null) {
				System.out.println("  Token \"" + account.getTokenId() + "\" not found.");
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
