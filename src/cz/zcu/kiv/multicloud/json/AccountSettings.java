package cz.zcu.kiv.multicloud.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * cz.zcu.kiv.multicloud.json/AccountSettings.java
 *
 * Bean for holding information about user account registered by a cloud storage service provider. No user credentials are stored here.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountSettings {

	/** User identifier. */
	@JsonProperty("user_id")
	private String userId;

	/** Settings identifier. */
	@JsonProperty("settings_id")
	private String settingsId;
	/** Token identifier. */
	@JsonProperty("token_id")
	private String tokenId;

	/**
	 * Returns the identifier of the settings.
	 * @return Settings identifier.
	 */
	public String getSettingsId() {
		return settingsId;
	}

	/**
	 * Returns the identifier of the token.
	 * @return Token identifier.
	 */
	public String getTokenId() {
		return tokenId;
	}

	/**
	 * Returns the identifier of the user.
	 * @return User identifier.
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Determines if the user has authorized this client.
	 * @return If the client is authorized.
	 */
	@JsonIgnore
	public boolean isAuthorized() {
		return (tokenId != null);
	}

	/**
	 * Sets the identifier of the settings.
	 * @param settingsId Settings identifier.
	 */
	public void setSettingsId(String settingsId) {
		this.settingsId = settingsId;
	}

	/**
	 * Sets the identifier of the token.
	 * @param tokenId Token identifier.
	 */
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	/**
	 * Sets the identifier of the user.
	 * @param userId User identifier.
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
