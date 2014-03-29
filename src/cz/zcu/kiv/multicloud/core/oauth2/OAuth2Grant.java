package cz.zcu.kiv.multicloud.core.oauth2;

public interface OAuth2Grant {

	void setup(OAuth2Settings settings);
	
	AuthorizationRequest authorize();
		
	OAuth2Token getToken();
	
	OAuth2Error getError();
	
}
