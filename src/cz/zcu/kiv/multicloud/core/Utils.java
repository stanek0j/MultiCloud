package cz.zcu.kiv.multicloud.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * cz.zcu.kiv.multicloud.core/Utils.java
 *
 * General purpose methods for use in the multicloud core.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Utils {

	/**
	 * Determines if the supplied character belongs to the group of letters allowed in URI.
	 * Allowed letters conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to group identified as ALPHA.
	 * ALPHA is defined as hexadecimal ranges 0x41-0x5A and 0x61-0x7A.
	 * @param c Tested character.
	 * @return If it is allowed in URI.
	 */
	public static boolean isUriLetter(char c) {
		return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')));
	}
	
	/**
	 * Determines if the supplied character belongs to the group of digits allowed in URI.
	 * Allowed digits conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to group identified as DIGIT.
	 * DIGIT is defined as hexadecimal range 0x30-0x39.
	 * @param c Tested character.
	 * @return If it is allowed in URI. 
	 */
	public static boolean isUriDigit(char c) {
		return ((c >= '0') && (c <= '9'));
	}
	
	/**
	 * Determines if the supplied character belongs to the group of letters or digits allowed in URI.
	 * Allowed letters and digits conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to groups identified as ALPHA and DIGIT.
	 * ALPHA is defined as hexadecimal ranges 0x41-0x5A and 0x61-0x7A.
	 * DIGIT is defined as hexadecimal range 0x30-0x39.
	 * @param c Tested character.
	 * @return If it is allowed in URI.
	 */
	public static boolean isUriLetterOrDigit(char c) {
		return (isUriLetter(c) || isUriDigit(c));
	}
	
	/**
	 * Converts a {@link java.util.Map} to a {@link java.util.List} of {@link org.apache.http.NameValuePair} objects. 
	 * @param map Map to be converted.
	 * @return List of NameValuePair objects.
	 */
	public static List<NameValuePair> mapToList(Map<String, Object> map) {
		List<NameValuePair> list = new LinkedList<>();
		for (Entry<String, Object> entry: map.entrySet()) {
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
		}
		return list;
	}
	
	/**
	 * Check if the supplied string is null, zero length or contains only whitespace characters.
	 * @param s Tested string.
	 * @return If it is null or empty.
	 */
	public static boolean isNullOrEmpty(String s) {
		return (s == null || s.isEmpty() || s.trim().isEmpty());
	}
	
}
