package cz.zcu.kiv.multicloud.core.oauth2;

import org.simpleframework.http.core.Container;

public class AuthorizationCodeGrant implements OAuth2Grant {

	private Container server;
	
	public AuthorizationCodeGrant() {
		server = RedirectServer.getInstance();
	}

	@Override
	public void setup(OAuth2Settings settings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AuthorizationRequest authorize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuth2Token getToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuth2Error getError() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
