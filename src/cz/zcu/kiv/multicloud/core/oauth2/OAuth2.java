package cz.zcu.kiv.multicloud.core.oauth2;

public class OAuth2 {

	public static final String VERSION = "OAuth 2.0";

	private OAuth2Settings settings;
	private CredentialStore store;
	
	private String state;
	
	public OAuth2() {
		this(null, null);
	}
	
	public OAuth2(OAuth2Settings settings) {
		this(settings, null);
	}
	
	public OAuth2(CredentialStore store) {
		this(null, store);
	}
	
	public OAuth2(OAuth2Settings settings, CredentialStore store) {
		this.settings = settings;
		this.store = store;
	}

	public OAuth2Settings getSettings() {
		return settings;
	}

	public void setSettings(OAuth2Settings settings) {
		this.settings = settings;
	}
	
	public CredentialStore getStore() {
		return store;
	}

	public void setStore(CredentialStore store) {
		this.store = store;
	}
	
	public String getState() {
		return state;
	}

	public String getVersion() {
		return VERSION;
	}
}
