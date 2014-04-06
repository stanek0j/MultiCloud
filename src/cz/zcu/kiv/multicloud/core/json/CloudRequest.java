package cz.zcu.kiv.multicloud.core.json;

import cz.zcu.kiv.multicloud.core.HttpMethod;

/**
 * cz.zcu.kiv.multicloud.core.json/CloudRequest.java
 *
 * Bean for holding basic information about a request to the cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudRequest {

	/** URI of the request. */
	private String uri;
	/** HTTP method of the request. */
	private HttpMethod method;

	/**
	 * Returns the HTTP method of the request.
	 * @return HTTP method of the request.
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Returns the URI of the request.
	 * @return URI of the request.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the HTTP method of the request.
	 * @param method HTTP method of the request.
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/**
	 * Sets the URI of the request.
	 * @param uri URI of the request.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

}
