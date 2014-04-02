package cz.zcu.kiv.multicloud.core.oauth2;

/**
 * cz.zcu.kiv.multicloud.core.oauth2/OAuth2ErrorType.java
 *
 * List of all the error types that can occur in a response from the authorization server.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum OAuth2ErrorType {

	SUCCESS,
	INVALID_REQUEST,
	UNAUTHORIZED_CLIENT,
	ACCESS_DENIED,
	UNSUPPORTED_RESPONSE_TYPE,
	INVALID_SCOPE,
	SERVER_ERROR,
	TEMPORARILY_UNAVAILABLE,
	INVALID_CLIENT,
	INVALID_GRANT,
	UNSUPPORTED_GRANT_TYPE

}
