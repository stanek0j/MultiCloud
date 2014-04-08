package cz.zcu.kiv.multicloud.json;

import java.util.Map;

import cz.zcu.kiv.multicloud.utils.HttpMethod;

/**
 * cz.zcu.kiv.multicloud.json/CloudRequest.java
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
	/** Parameters of the request. */
	private Map<String, String> params;

	/**
	 * Returns the HTTP method of the request.
	 * @return HTTP method of the request.
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Return the list of all parameters of the request.
	 * @return List of parameters.
	 */
	public Map<String, String> getParams() {
		return params;
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
	 * Sets the list of all parameters of the request.
	 * @param params List of parameters.
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * Sets the URI of the request.
	 * @param uri URI of the request.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

}
