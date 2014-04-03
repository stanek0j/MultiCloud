package cz.zcu.kiv.multicloud.core.oauth2;

import java.io.IOException;

import cz.zcu.kiv.multicloud.core.CredentialStore;
import cz.zcu.kiv.multicloud.core.Utils;

/**
 * cz.zcu.kiv.multicloud.core.oauth2/OAuth2.java
 *
 * Implementation of the <a href="http://tools.ietf.org/html/rfc6749">OAuth 2.0 authorization framework</a>.
 * This implementation is specifically designed for the use with cloud storage services, but it also might be suitable for other uses.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OAuth2 {

	/** Version of the OAuth implementation */
	public static final String VERSION = "Version 1.0 of OAuth 2.0 implementation.";

	/** Authorization callback. */
	protected AuthorizationCallback authCallback;
	/** Settings to use. */
	protected OAuth2Settings settings;
	/** Credential store to use. */
	protected CredentialStore store;

	/** Currently used grant. */
	private OAuth2Grant grant;
	/** Identifier returned after storing an access token. */
	private String obtainedStoreKey;

	/**
	 * Empty ctor.
	 */
	public OAuth2() {
		this(null, null, null);
	}

	/**
	 * Ctor with authorization callback.
	 * @param authCallback Authorization callback.
	 */
	public OAuth2(AuthorizationCallback authCallback) {
		this(authCallback, null, null);
	}

	/**
	 * Ctor with authorization callback and credential store to use.
	 * @param authCallback Authorization callback.
	 * @param store Credential store.
	 */
	public OAuth2(AuthorizationCallback authCallback, CredentialStore store) {
		this(authCallback, null, store);
	}

	/**
	 * Ctor with authorization callback and settings to use.
	 * @param authCallback Authorization callback.
	 * @param settings The settings.
	 */
	public OAuth2(AuthorizationCallback authCallback, OAuth2Settings settings) {
		this(authCallback, settings, null);
	}

	/**
	 * Ctor with authorization callback, settings and credential store to use.
	 * @param authCallback Authorization callback.
	 * @param settings The settings.
	 * @param store Credential store.
	 */
	public OAuth2(AuthorizationCallback authCallback, OAuth2Settings settings, CredentialStore store) {
		this.authCallback = authCallback;
		this.settings = settings;
		this.store = store;
		this.grant = null;
		this.obtainedStoreKey = null;
	}

	/**
	 * Ctor with credential store to use.
	 * @param store Credential store.
	 */
	public OAuth2(CredentialStore store) {
		this(null, null, store);
	}

	/**
	 * Ctor with settings to use.
	 * @param settings The settings.
	 */
	public OAuth2(OAuth2Settings settings) {
		this(null, settings, null);
	}

	/**
	 * Ctor with settings and credential store to use.
	 * @param settings The settings.
	 * @param store Credential store.
	 */
	public OAuth2(OAuth2Settings settings, CredentialStore store) {
		this(null, settings, store);
	}

	/**
	 * Handles the whole process of requesting for authorization and stores the received access token in the credential store.
	 * @param storeKey Key used for storing the access token.
	 * @return Error occurred during the process.
	 * @throws OAuth2SettingsException Exception when not proper settings are passed.
	 */
	public OAuth2Error authorize(String storeKey) throws OAuth2SettingsException {
		OAuth2Token token = null;
		OAuth2Error error = null;
		obtainedStoreKey = null;

		/* create an instance of the grant implementation */
		switch (settings.getGrantType()) {
		case AUTHORIZATION_CODE_GRANT:
			grant = new AuthorizationCodeGrant();
			break;
		case IMPLICIT_GRANT:
			grant = new ImplicitGrant();
			break;
		case RESOURCE_OWNER_PASSWORD_CREDENTIAL_GRANT:
			grant = new ResOwnerPassCredGrant();
			break;
		case CLIENT_CREDENTIAL_GRANT:
			grant = new ClientCredGrant();
			break;
		case EXTENSION_GRANT:
			if (settings.getGrantClass() != null) {
				try {
					grant = settings.getGrantClass().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			break;
		}

		/* follow the generic authorization flow */
		if (grant != null) {
			grant.setup(settings);
			AuthorizationRequest request = grant.authorize();
			if (request.isActionRequied()) {
				if (authCallback != null) {
					authCallback.onAuthorizationRequest(request);
				} else {
					System.out.println(request);
				}
			}
			token = grant.getToken();
			error = grant.getError();

			/* store the acquired access token */
			if (store != null && error.getType() == OAuth2ErrorType.SUCCESS) {
				if (Utils.isNullOrEmpty(storeKey)) {
					obtainedStoreKey = store.storeCredential(token);
				} else {
					obtainedStoreKey = storeKey;
					store.storeCredential(storeKey, token);
				}
			}
			close();
		}

		return error;
	}

	/**
	 * Closes all the resources used by the grant.
	 */
	public void close() {
		if (grant != null) {
			try {
				grant.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			grant = null;
		}
	}

	/**
	 * Returns the authorization callback.
	 * @return Authorization callback.
	 */
	public AuthorizationCallback getAuthCallback() {
		return authCallback;
	}

	/**
	 * Returns the last identifier returned from the credential store after storing an access token.
	 * @return Identifier.
	 */
	public String getObtainedStoreKey() {
		return obtainedStoreKey;
	}

	/**
	 * Returns the settings of the authorization.
	 * @return The settings.
	 */
	public OAuth2Settings getSettings() {
		return settings;
	}

	/**
	 * Returns the credential store used.
	 * @return Credential store.
	 */
	public CredentialStore getStore() {
		return store;
	}

	/**
	 * Returns the version of the OAuth implementation.
	 * @return OAuth implementation version.
	 */
	public String getVersion() {
		return VERSION;
	}

	/**
	 * 
	 * @param storeKey Key used for storing the access token.
	 * @return Error occurred during the process.
	 * @throws OAuth2SettingsException Exception when not proper settings are passed.
	 */
	public OAuth2Error refresh(String storeKey) throws OAuth2SettingsException {
		if (store == null) {
			throw new OAuth2SettingsException("Credential store not supplied.");
		}
		OAuth2Token token = store.retrieveCredential(storeKey);
		if (token == null) {
			throw new OAuth2SettingsException("No token retrieved from the credential store.");
		}

		OAuth2Error error = null;
		obtainedStoreKey = null;

		/* create an instance of the grant implementation */
		grant = new RefreshTokenGrant();

		/* follow the generic authorization flow */
		if (grant != null) {
			settings.setRefreshToken(token.getRefreshToken());
			grant.setup(settings);
			AuthorizationRequest request = grant.authorize();
			if (request.isActionRequied()) {
				if (authCallback != null) {
					authCallback.onAuthorizationRequest(request);
				} else {
					System.out.println(request);
				}
			}
			OAuth2Token tokenUpdate = grant.getToken();
			error = grant.getError();

			/* update and store the refreshed token */
			if (store != null && error.getType() == OAuth2ErrorType.SUCCESS) {
				token.setType(tokenUpdate.getType());
				if (!Utils.isNullOrEmpty(tokenUpdate.getAccessToken())) {
					token.setAccessToken(tokenUpdate.getAccessToken());
				}
				if (!Utils.isNullOrEmpty(tokenUpdate.getRefreshToken())) {
					token.setRefreshToken(tokenUpdate.getRefreshToken());
				}
				if (!Utils.isNullOrEmpty(tokenUpdate.getKeyId())) {
					token.setKeyId(tokenUpdate.getKeyId());
				}
				if (!Utils.isNullOrEmpty(tokenUpdate.getMacKey())) {
					token.setMacKey(tokenUpdate.getMacKey());
				}
				if (!Utils.isNullOrEmpty(tokenUpdate.getMacAlgorithm())) {
					token.setMacAlgorithm(tokenUpdate.getMacAlgorithm());
				}
				if (tokenUpdate.getExpiresIn() > -1) {
					token.setExpiresIn(tokenUpdate.getExpiresIn());
				}
				token.setTimestamp(System.currentTimeMillis() / 1000);
				store.storeCredential(storeKey, token);
			}
			close();
		}

		return error;
	}

	/**
	 * Sets the authorization callback.
	 * @param authCallback Authorization callback.
	 */
	public void setAuthCallback(AuthorizationCallback authCallback) {
		this.authCallback = authCallback;
	}

	/**
	 * Sets the settings of the authorization.
	 * @param settings The settings.
	 */
	public void setSettings(OAuth2Settings settings) {
		this.settings = settings;
	}

	/**
	 * Sets the credential store to use.
	 * @param store Credential store.
	 */
	public void setStore(CredentialStore store) {
		this.store = store;
	}

}
