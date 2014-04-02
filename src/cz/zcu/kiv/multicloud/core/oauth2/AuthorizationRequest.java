package cz.zcu.kiv.multicloud.core.oauth2;

/**
 * cz.zcu.kiv.multicloud.core.oauth2/AuthorizationRequest.java
 *
 * Represents an authorization request of the <a href="http://tools.ietf.org/html/rfc6749">OAuth 2.0 specification</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AuthorizationRequest {

	/** URI to be requested. */
	private final String requestUri;

	/**
	 * Ctor.
	 * @param requestUri URI to be requested.
	 */
	public AuthorizationRequest(String requestUri) {
		this.requestUri = requestUri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (requestUri != null && !requestUri.isEmpty()) {
			return "To authorize this application, visit:" + System.getProperty("line.separator") + requestUri;
		} else {
			return "No action required.";
		}
	}

}
