package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileCloudPair.java
 *
 * Class for holding file information and the settings for downloading it from a cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileCloudPair {

	/** File information. */
	private FileInfo file;
	/** Cloud request settings. */
	private CloudRequest request;

	/**
	 * Empty ctor.
	 */
	public FileCloudPair() {
		file = null;
		request = null;
	}

	/**
	 * Ctro with file information and cloud request settings supplied.
	 * @param file File information.
	 * @param request Cloud request settings.
	 */
	public FileCloudPair(FileInfo file, CloudRequest request) {
		this.file = file;
		this.request = request;
	}

	/**
	 * Returns the file information.
	 * @return File information.
	 */
	public FileInfo getFile() {
		return file;
	}

	/**
	 * Returns the cloud request settings.
	 * @return Cloud request settings.
	 */
	public CloudRequest getRequest() {
		return request;
	}

	/**
	 * Sets the file information.
	 * @param file File information.
	 */
	public void setFile(FileInfo file) {
		this.file = file;
	}

	/**
	 * Sets the cloud request settings.
	 * @param request Cloud request settings.
	 */
	public void setRequest(CloudRequest request) {
		this.request = request;
	}

}
