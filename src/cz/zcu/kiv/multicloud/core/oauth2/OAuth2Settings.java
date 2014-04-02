package cz.zcu.kiv.multicloud.core.oauth2;

import java.util.HashMap;
import java.util.Map;

public class OAuth2Settings {

	private OAuth2GrantType grantType;

	private String clientId;
	private String clientSecret;
	private String authorizeUri;
	private String redirectUri;
	private String tokenUri;
	private String scope;
	
	private Map<String, String> extraAuthorizeParams;
	private Map<String, String> extraTokenParams;
	
	public OAuth2Settings() {
		extraAuthorizeParams = new HashMap<>();
		extraTokenParams = new HashMap<>();
	}
	
	public OAuth2GrantType getGrantType() {
		return grantType;
	}
	
	public void setGrantType(OAuth2GrantType grantType) {
		this.grantType = grantType;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}
	
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public String getAuthorizeUri() {
		return authorizeUri;
	}

	public void setAuthorizeUri(String authorizeUri) {
		this.authorizeUri = authorizeUri;
	}
	
	public String getRedirectUri() {
		return redirectUri;
	}
	
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public void setTokenUri(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}

	public Map<String, String> getExtraAuthorizeParams() {
		return extraAuthorizeParams;
	}

	public void addExtraAuthorizeParams(String key, String value) {
		extraAuthorizeParams.put(key, value);
	}
	
	public Map<String, String> getExtraTokenParams() {
		return extraTokenParams;
	}
	
	public void addExtraTokenParams(String key, String value) {
		extraTokenParams.put(key, value);
	}
}
