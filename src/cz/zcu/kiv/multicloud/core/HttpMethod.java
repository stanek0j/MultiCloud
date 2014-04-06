package cz.zcu.kiv.multicloud.core;

/**
 * cz.zcu.kiv.multicloud.core/HttpMethod.java
 *
 * List of all methods of the HTTP/1.1 protocol as defined in the <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a>.
 * It also contains the PATCH method defined in <a href="http://tools.ietf.org/html/rfc2068">RFC 2068</a> since it is used in some of the implementations of cloud storage services.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum HttpMethod {

	OPTIONS,
	GET,
	HEAD,
	POST,
	PUT,
	DELETE,
	TRACE,
	CONNECT,
	PATCH

}
