package cz.zcu.kiv.multicloud.core.oauth2;

public class OAuth2 {

	public static final String VERSION = "OAuth 2.0";

	private OAuth2Settings settings;
	private CredentialStore store;

	public OAuth2() {
		this(null, null);
	}

	public OAuth2(CredentialStore store) {
		this(null, store);
	}

	public OAuth2(OAuth2Settings settings) {
		this(settings, null);
	}

	public OAuth2(OAuth2Settings settings, CredentialStore store) {
		this.settings = settings;
		this.store = store;
	}

	public OAuth2Settings getSettings() {
		return settings;
	}

	public CredentialStore getStore() {
		return store;
	}

	public String getVersion() {
		return VERSION;
	}

	public void setSettings(OAuth2Settings settings) {
		this.settings = settings;
	}

	public void setStore(CredentialStore store) {
		this.store = store;
	}
}
