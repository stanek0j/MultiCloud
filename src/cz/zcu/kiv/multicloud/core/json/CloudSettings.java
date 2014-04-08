package cz.zcu.kiv.multicloud.core.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import cz.zcu.kiv.multicloud.core.oauth2.OAuth2GrantType;

/**
 * cz.zcu.kiv.multicloud.core.json/CloudSettings.java
 *
 * Bean for holding complete information about how to communicate with a cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudSettings {

	/** Settings identifier. */
	@JsonProperty("name")
	private String settingsId;
	/** Description of the settings. */
	@JsonProperty("description")
	private String settingsDescription;

	/** Authorization request parameters. */
	@JsonProperty("authorize_request")
	private CloudRequest authorizeRequest;
	/** Token request parameters. */
	@JsonProperty("token_request")
	private CloudRequest tokenRequest;
	/** Grant type for authorization. */
	@JsonProperty("grant_type")
	private OAuth2GrantType grantType;
	/** Client ID used in authorization. */
	@JsonProperty("client_id")
	private String clientId;
	/** Client secret used in authorization. */
	@JsonProperty("client_secret")
	private String clientSecret;
	/** Redirect URI used in authorization. */
	@JsonProperty("redirect_uri")
	private String redirectUri;
	/** Scope for accessing the cloud storage service. */
	private String scope;

	/** Account info request parameters. */
	@JsonProperty("account_info_request")
	private CloudRequest accountInfoRequest;
	/** File download request parameters. */
	@JsonProperty("download_file_request")
	private CloudRequest downloadFileRequest;
	/** File upload request parameters. */
	@JsonProperty("upload_file_request")
	private CloudRequest uploadFileRequest;
	/** Folder create request parameters. */
	@JsonProperty("create_dir_request")
	private CloudRequest createDirRequest;
	/** Folder list request parameters. */
	@JsonProperty("list_dir_request")
	private CloudRequest listDirRequest;
	/** Rename request parameters. */
	@JsonProperty("rename_request")
	private CloudRequest renameRequest;
	/** Copy request parameters. */
	@JsonProperty("copy_request")
	private CloudRequest copyRequest;
	/** Move request parameters. */
	@JsonProperty("move_request")
	private CloudRequest moveRequest;
	/** Delete request parameters. */
	@JsonProperty("delete_request")
	private CloudRequest deleteRequest;

	/**
	 * Returns the account information request parameters.
	 * @return Account information request parameters.
	 */
	public CloudRequest getAccountInfoRequest() {
		return accountInfoRequest;
	}

	/**
	 * Returns the authorization request parameters.
	 * @return Authorization request parameters.
	 */
	public CloudRequest getAuthorizeRequest() {
		return authorizeRequest;
	}

	/**
	 * Returns the client ID used in authorization.
	 * @return Client ID.
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Returns the client secret used in authorization.
	 * @return Client secret.
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * Returns the copy request parameters.
	 * @return Copy request parameters.
	 */
	public CloudRequest getCopyRequest() {
		return copyRequest;
	}

	/**
	 * Returns the folder creation request parameters.
	 * @return Folder creation request parameters.
	 */
	public CloudRequest getCreateDirRequest() {
		return createDirRequest;
	}

	/**
	 * Returns the delete request parameters.
	 * @return Delete request parameters.
	 */
	public CloudRequest getDeleteRequest() {
		return deleteRequest;
	}

	/**
	 * Returns the file download request parameters.
	 * @return File download request parameters.
	 */
	public CloudRequest getDownloadFileRequest() {
		return downloadFileRequest;
	}

	/**
	 * Returns the grant type for the authorization.
	 * @return Grant type.
	 */
	public OAuth2GrantType getGrantType() {
		return grantType;
	}

	/**
	 * Returns the folder list request parameters.
	 * @return Folder list request parameters.
	 */
	public CloudRequest getListDirRequest() {
		return listDirRequest;
	}

	/**
	 * Returns the move request parameters.
	 * @return Move request parameters.
	 */
	public CloudRequest getMoveRequest() {
		return moveRequest;
	}

	/**
	 * Returns the redirect URI.
	 * @return Redirect URI.
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * Returns the rename request parameters.
	 * @return Rename request parameters.
	 */
	public CloudRequest getRenameRequest() {
		return renameRequest;
	}

	/**
	 * Returns the scope for accessing the cloud storage service.
	 * @return Scope.
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Returns the description of the settings.
	 * @return Settings description.
	 */
	public String getSettingsDescription() {
		return settingsDescription;
	}

	/**
	 * Returns the identifier of the settings.
	 * @return Settings identifier.
	 */
	public String getSettingsId() {
		return settingsId;
	}

	/**
	 * Returns the token request parameters.
	 * @return Token request parameters.
	 */
	public CloudRequest getTokenRequest() {
		return tokenRequest;
	}

	/**
	 * Returns the file upload request parameters.
	 * @return File upload request parameters.
	 */
	public CloudRequest getUploadFileRequest() {
		return uploadFileRequest;
	}

	/**
	 * Sets the account information request parameters.
	 * @param accountInfoRequest Account information request parameters.
	 */
	public void setAccountInfoRequest(CloudRequest accountInfoRequest) {
		this.accountInfoRequest = accountInfoRequest;
	}

	/**
	 * Sets the authorization request parameters.
	 * @param authorizeRequest Authorization request parameters.
	 */
	public void setAuthorizeRequest(CloudRequest authorizeRequest) {
		this.authorizeRequest = authorizeRequest;
	}

	/**
	 * Sets the client ID used in authorization.
	 * @param clientId Client ID.
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Sets the client secret used in authorization.
	 * @param clientSecret Client secret.
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * Sets the copy request parameters.
	 * @param copyRequest Copy request parameters.
	 */
	public void setCopyRequest(CloudRequest copyRequest) {
		this.copyRequest = copyRequest;
	}

	/**
	 * Sets the folder creation request parameters.
	 * @param createDirRequest Folder creation request parameters.
	 */
	public void setCreateDirRequest(CloudRequest createDirRequest) {
		this.createDirRequest = createDirRequest;
	}

	/**
	 * Sets the delete request parameters.
	 * @param deleteRequest Delete request parameters.
	 */
	public void setDeleteRequest(CloudRequest deleteRequest) {
		this.deleteRequest = deleteRequest;
	}

	/**
	 * Sets the file download request parameters.
	 * @param downloadFileRequest File download request parameters.
	 */
	public void setDownloadFileRequest(CloudRequest downloadFileRequest) {
		this.downloadFileRequest = downloadFileRequest;
	}

	/**
	 * Sets the grant type for the authorization.
	 * @param grantType Grant type.
	 */
	public void setGrantType(OAuth2GrantType grantType) {
		this.grantType = grantType;
	}

	/**
	 * Sets the folder list request parameters.
	 * @param listDirRequest Folder list request parameters.
	 */
	public void setListDirRequest(CloudRequest listDirRequest) {
		this.listDirRequest = listDirRequest;
	}

	/**
	 * Sets the move request parameters.
	 * @param moveRequest Move request parameters.
	 */
	public void setMoveRequest(CloudRequest moveRequest) {
		this.moveRequest = moveRequest;
	}

	/**
	 * Sets the redirect URI.
	 * @param redirectUri Redirect URI.
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * Sets the rename request parameters.
	 * @param renameRequest Rename request parameters.
	 */
	public void setRenameRequest(CloudRequest renameRequest) {
		this.renameRequest = renameRequest;
	}

	/**
	 * Sets the scope for accessing the cloud storage service.
	 * @param scope Scope.
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * Sets the description of the settings.
	 * @param settingsDescription Settings description.
	 */
	public void setSettingsDescription(String settingsDescription) {
		this.settingsDescription = settingsDescription;
	}

	/**
	 * Sets the identifier of the settings.
	 * @param settingsId Settings identifier.
	 */
	public void setSettingsId(String settingsId) {
		this.settingsId = settingsId;
	}

	/**
	 * Sets the token request parameters.
	 * @param tokenRequest Token request parameters.
	 */
	public void setTokenRequest(CloudRequest tokenRequest) {
		this.tokenRequest = tokenRequest;
	}

	/**
	 * Sets the file upload request parameters.
	 * @param uploadFileRequest File upload request parameters.
	 */
	public void setUploadFileRequest(CloudRequest uploadFileRequest) {
		this.uploadFileRequest = uploadFileRequest;
	}

}
