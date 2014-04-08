package cz.zcu.kiv.multicloud.core;

/**
 * cz.zcu.kiv.multicloud.core/Core.java
 *
 * The MultiCloud core.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Core {

	/** The version of MultiCloud core. */
	public static final String VERSION = "1.0";

	/**
	 * Main method that prints out core version.
	 * @param args Start arguments.
	 */
	public static void main(String[] args) {
		System.out.println("MultiCloud core version: " + VERSION);
		System.exit(0);
	}

}
