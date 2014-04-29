package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileCloudSource.java
 *
 * Class for holding file information, access token and the settings for downloading it from a cloud storage service.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileCloudSource {

	/** Account name. */
	private String accountName;
	/** File information. */
	private FileInfo file;
	/** Cloud request settings. */
	private CloudRequest request;
	/** Access token for the cloud storage service. */
	private OAuth2Token token;

	/**
	 * Empty ctor.
	 */
	public FileCloudSource() {
		accountName = null;
		file = null;
		request = null;
		token = null;
	}

	/**
	 * Ctro with file information, access token and cloud request settings supplied.
	 * @param accountName Account name.
	 * @param file File information.
	 * @param request Cloud request settings.
	 * @param token Access token for the cloud storage service.
	 */
	public FileCloudSource(String accountName, FileInfo file, CloudRequest request, OAuth2Token token) {
		this.accountName = accountName;
		this.file = file;
		this.request = request;
		this.token = token;
	}

	/**
	 * Returns the account name.
	 * @return Account name.
	 */
	public String getAccountName() {
		return accountName;
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
	 * Returns the access token for the storage service.
	 * @return Access token for the storage service.
	 */
	public OAuth2Token getToken() {
		return token;
	}

	/**
	 * Sets the account name.
	 * @param accountName Account name.
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
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

	/**
	 * Sets the access token for the storage service.
	 * @param token Access token for the storage service.
	 */
	public void setToken(OAuth2Token token) {
		this.token = token;
	}

}
