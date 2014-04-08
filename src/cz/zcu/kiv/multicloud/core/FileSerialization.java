package cz.zcu.kiv.multicloud.core;

/**
 * cz.zcu.kiv.multicloud.core/FileSerialization.java
 *
 * List of supported serialization schemes for storing data in a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum FileSerialization {

	OBJECT,				// use default Java Object serialization
	JSON				// use Jackson JSON serialization

}