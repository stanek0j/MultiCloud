package cz.zcu.kiv.multicloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.multicloud.filesystem.AccountInfoOp;
import cz.zcu.kiv.multicloud.filesystem.AccountQuotaOp;
import cz.zcu.kiv.multicloud.filesystem.CopyOp;
import cz.zcu.kiv.multicloud.filesystem.DeleteOp;
import cz.zcu.kiv.multicloud.filesystem.FileCloudSource;
import cz.zcu.kiv.multicloud.filesystem.FileDownloadOp;
import cz.zcu.kiv.multicloud.filesystem.FileType;
import cz.zcu.kiv.multicloud.filesystem.FileUploadOp;
import cz.zcu.kiv.multicloud.filesystem.FolderCreateOp;
import cz.zcu.kiv.multicloud.filesystem.FolderListOp;
import cz.zcu.kiv.multicloud.filesystem.MoveOp;
import cz.zcu.kiv.multicloud.filesystem.Operation;
import cz.zcu.kiv.multicloud.filesystem.ProgressListener;
import cz.zcu.kiv.multicloud.filesystem.RenameOp;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.OperationError;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.OAuth2;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Error;
import cz.zcu.kiv.multicloud.oauth2.OAuth2ErrorType;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.CredentialStore;
import cz.zcu.kiv.multicloud.utils.FileAccountManager;
import cz.zcu.kiv.multicloud.utils.FileCloudManager;
import cz.zcu.kiv.multicloud.utils.FileCredentialStore;
import cz.zcu.kiv.multicloud.utils.SecureFileCredentialStore;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud/MultiCloud.java			<br /><br />
 *
 * The MultiCloud library.
 * All methods are synchronous. For asynchronous usage, external threading should be used.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloud {

	/** Version of the library. */
	public static final String VERSION = "1.0";

	/**
	 * Main method to return the version of the library.
	 * @param args Arguments passed.
	 */
	public static void main(String[] args) {
		System.out.println("====================");
		System.out.println(" MultiCloud library ");
		System.out.println("====================");
		System.out.println("   Version: " + VERSION);
		System.exit(1);
	}

	/** Cloud settings manager. */
	private CloudManager cloudManager;
	/** Credential store. */
	private CredentialStore credentialStore;
	/** User account manager. */
	private AccountManager accountManager;
	/** Last error that occurred during any operation. */
	private OperationError lastError;
	/** List of sources for downloading a file from. */
	private List<FileCloudSource> fileMultiDownloadSources;

	/** Listener of the transfer progress. */
	private ProgressListener listener;
	/** Currently running operation. */
	private Operation<?> op;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/**
	 * Empty ctor.
	 */
	public MultiCloud() {
		credentialStore = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		FileCloudManager cm = FileCloudManager.getInstance();
		try {
			cm.loadCloudSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cloudManager = cm;
		FileAccountManager um = FileAccountManager.getInstance();
		try {
			um.loadAccountSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		accountManager = um;
		lastError = null;
		fileMultiDownloadSources = new ArrayList<>();
		lock = new Object();
	}

	/**
	 * Ctor with custom settings supplied.
	 * @param settings Custom settings.
	 */
	public MultiCloud(MultiCloudSettings settings) {
		if (settings.getCredentialStore() == null) {
			credentialStore = new SecureFileCredentialStore(FileCredentialStore.DEFAULT_STORE_FILE);
		} else {
			credentialStore = settings.getCredentialStore();
		}
		if (settings.getCloudManager() == null) {
			FileCloudManager cm = FileCloudManager.getInstance();
			try {
				cm.loadCloudSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cloudManager = cm;
		} else {
			cloudManager = settings.getCloudManager();
		}
		if (settings.getAccountManager() == null) {
			FileAccountManager um = FileAccountManager.getInstance();
			try {
				um.loadAccountSettings();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			accountManager = settings.getAccountManager();
		}
		lastError = null;
		lock = new Object();
	}

	/**
	 * Aborts the currently running operation, if any.
	 */
	public void abortOperation() {
		synchronized (lock) {
			if (op != null) {
				op.abort();
			}
		}
	}

	/**
	 * Retrieve basic information about the user. Information consists of user name and identifier.
	 * @param accountName Name of the user account.
	 * @return Information about the user.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public AccountInfo accountInfo(String accountName) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		synchronized (lock) {
			op = new AccountInfoOp(token, settings.getAccountInfoRequest());
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		AccountInfo result = ((AccountInfoOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Retrieve information about the quota associated with the user account.
	 * @param accountName Name of the user account.
	 * @return Quota information.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public AccountQuota accountQuota(String accountName) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		synchronized (lock) {
			op = new AccountQuotaOp(token, settings.getAccountQuotaRequest());
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		AccountQuota result = ((AccountQuotaOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Adds a new source for download from multiple cloud storage services.
	 * @param accountName Name of the user account.
	 * @param sourceFile File to be downloaded.
	 * @throws MultiCloudException If the operation failed.
	 */
	public void addDownloadSource(String accountName, FileInfo sourceFile) throws MultiCloudException {
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		fileMultiDownloadSources.add(new FileCloudSource(accountName, sourceFile, settings.getDownloadFileRequest(), token));
	}

	/**
	 * Runs the authorization process for the specified user account.
	 * For certain authorization flows, this operation is blocking.
	 * @param accountName Name of the user account.
	 * @param callback Callback from the authorization process, if necessary.
	 * @throws MultiCloudException If some parameters were wrong.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the authorization process was interrupted.
	 */
	public void authorizeAccount(String accountName, AuthorizationCallback callback) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (account.isAuthorized()) {
			throw new MultiCloudException("User account already authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2 auth = new OAuth2(Utils.cloudSettingsToOAuth2Settings(settings), credentialStore);
		if (callback != null) {
			auth.setAuthCallback(callback);
		}
		OAuth2Error error = auth.authorize(null);
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			throw new MultiCloudException("Authorization failed.");
		} else {
			if (!Utils.isNullOrEmpty(account.getTokenId())) {
				credentialStore.deleteCredential(account.getTokenId());
			}
			account.setTokenId(auth.getObtainedStoreKey());
		}
		accountManager.saveAccountSettings();
	}

	/**
	 * Copy existing file or folder to new destination.
	 * @param accountName Name of the user account.
	 * @param file Original file or folder to be copied.
	 * @param destination Folder to copy the source to.
	 * @param destinationName File or folder name in the destination location. Null to retain original.
	 * @return File or folder information after copying.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo copy(String accountName, FileInfo file, FileInfo destination, String destinationName) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		if (file == null) {
			throw new MultiCloudException("File or folder must be supplied.");
		}
		if (destination == null) {
			throw new MultiCloudException("Destination folder must be supplied.");
		}
		if (destination.getFileType() != FileType.FOLDER) {
			throw new MultiCloudException("Destination must be a folder.");
		}
		synchronized (lock) {
			op = new CopyOp(token, settings.getCopyRequest(), file, destination, destinationName);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((CopyOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Creates new user account and stores it in the account manager.
	 * @param accountName Name for the user account.
	 * @param cloudStorage Name of the cloud storage service provider.
	 * @throws MultiCloudException If the given parameters were wrong.
	 */
	public void createAccount(String accountName, String cloudStorage) throws MultiCloudException {
		if (accountManager.getAccountSettings(accountName) != null) {
			throw new MultiCloudException("User account already exists.");
		}
		if (cloudManager.getCloudSettings(cloudStorage) == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		AccountSettings account = new AccountSettings();
		account.setUserId(accountName);
		account.setSettingsId(cloudStorage);
		accountManager.addAccountSettings(account);
	}

	/**
	 * Creates new folder in the specified location.
	 * @param accountName Name of the user account.
	 * @param folderName Name of the folder.
	 * @param parent Parent folder.
	 * @return Newly created folder.
	 * @throws MultiCloudException If the operation failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 * @throws OAuth2SettingsException If the authorization failed.
	 */
	public FileInfo createFolder(String accountName, String folderName, FileInfo parent) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		FileInfo useFolder = settings.getRootFolder();
		if (parent != null) {
			useFolder = parent;
		}
		if (useFolder.getFileType() != FileType.FOLDER) {
			throw new MultiCloudException("Supplied file instead of folder.");
		}
		synchronized (lock) {
			op = new FolderCreateOp(token, settings.getCreateDirRequest(), folderName, useFolder);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((FolderCreateOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Deletes the specified file or folder.
	 * @param accountName Name of the user account.
	 * @param file File or folder to be deleted.
	 * @return File information about the deleted file or folder.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo delete(String accountName, FileInfo file) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		if (file == null) {
			throw new MultiCloudException("File or folder must be supplied.");
		}
		synchronized (lock) {
			op = new DeleteOp(token, settings.getDeleteRequest(), file);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((DeleteOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Delete previously created user account, including associated token in the store.
	 * @param accountName Name of the user account.
	 * @throws MultiCloudException If the given parameter was wrong.
	 */
	public void deleteAccount(String accountName) throws MultiCloudException {
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account does not exist.");
		}
		if (account.isAuthorized()) {
			credentialStore.deleteCredential(account.getTokenId());
		}
		accountManager.removeAccountSettings(accountName);
	}

	/**
	 * Download file from a single destination.
	 * @param accountName Name of the user account.
	 * @param sourceFile File to be downloaded.
	 * @param destination Location to save the file to.
	 * @param overwrite If existing files should be overwritten.
	 * @return File downloaded.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public File downloadFile(String accountName, FileInfo sourceFile, File destination, boolean overwrite) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		File target = destination;
		if (destination.isDirectory()) {
			target = new File(destination, sourceFile.getName());
		}
		try {
			if (destination.exists() && !overwrite) {
				throw new MultiCloudException("Target file already exists.");
			} else {
				target.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(target, "rw");
			raf.setLength(sourceFile.getSize());
			raf.close();
		} catch (IOException e) {
			throw new MultiCloudException("Failed to create the target file.");
		}
		List<FileCloudSource> sources = new ArrayList<>();
		sources.add(new FileCloudSource(accountName, sourceFile, settings.getDownloadFileRequest(), token));
		synchronized (lock) {
			op = new FileDownloadOp(sources, target, listener);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		File result = ((FileDownloadOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Download file from a single destination.
	 * @param accountName Name of the user account.
	 * @param sourceFile File to be downloaded.
	 * @param destination Location to save the file to.
	 * @param overwrite If existing files should be overwritten.
	 * @return File downloaded.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public File downloadFile(String accountName, FileInfo sourceFile, String destination, boolean overwrite) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		return downloadFile(accountName, sourceFile, new File(destination), overwrite);
	}

	/**
	 * Download file from multiple cloud storage services.
	 * @param destination Location to save the file to.
	 * @param overwrite If existing files should be overwritten.
	 * @return File downloaded.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public File downloadMultiFile(File destination, boolean overwrite) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		if (fileMultiDownloadSources.size() == 0) {
			throw new MultiCloudException("No sources supplied.");
		}
		FileInfo sourceFile = fileMultiDownloadSources.get(0).getFile();
		File target = destination;
		if (destination.isDirectory()) {
			target = new File(destination, sourceFile.getName());
		}
		try {
			if (destination.exists() && !overwrite) {
				throw new MultiCloudException("Target file already exists.");
			} else {
				target.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(target, "rw");
			raf.setLength(sourceFile.getSize());
			raf.close();
		} catch (IOException e) {
			throw new MultiCloudException("Failed to create the target file.");
		}
		for (FileCloudSource source: fileMultiDownloadSources) {
			if (source.getToken().isExpired()) {
				refreshAccount(source.getAccountName(), null);
			}
		}
		synchronized (lock) {
			op = new FileDownloadOp(fileMultiDownloadSources, target, listener);
		}
		op.execute();
		fileMultiDownloadSources.clear();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		File result = ((FileDownloadOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Download file from multiple cloud storage services.
	 * @param destination Location to save the file to.
	 * @param overwrite If existing files should be overwritten.
	 * @return File downloaded.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public File downloadMultiFile(String destination, boolean overwrite) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		return downloadMultiFile(new File(destination), overwrite);
	}

	/**
	 * Returns the last error that occurred during any operation.
	 * @return Last error occurred.
	 */
	public OperationError getLastError() {
		return lastError;
	}

	/**
	 * Returns the progress listener.
	 * @return Progress listener.
	 */
	public ProgressListener getListener() {
		return listener;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.AccountManager}, {@link cz.zcu.kiv.multicloud.utils.CloudManager} and {@link cz.zcu.kiv.multicloud.utils.CredentialStore} used in this library instance.
	 * @return Settings used in the instance of the library.
	 */
	public MultiCloudSettings getSettings() {
		MultiCloudSettings settings = new MultiCloudSettings();
		settings.setAccountManager(accountManager);
		settings.setCloudManager(cloudManager);
		settings.setCredentialStore(credentialStore);
		return settings;
	}

	/**
	 * List the contents of the supplied folder.
	 * @param accountName Name of the user account.
	 * @param folder Folder to be listed.
	 * @return Folder contents.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo listFolder(String accountName, FileInfo folder) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		return listFolder(accountName, folder, false, false);
	}

	/**
	 * List the contents of the supplied folder.
	 * @param accountName Name of the user account.
	 * @param folder Folder to be listed.
	 * @param showDeleted If deleted content should be listed.
	 * @return Folder contents.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo listFolder(String accountName, FileInfo folder, boolean showDeleted) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		return listFolder(accountName, folder, showDeleted, false);
	}

	/**
	 * List the contents of the supplied folder.
	 * @param accountName Name of the user account.
	 * @param folder Folder to be listed.
	 * @param showDeleted If deleted content should be listed.
	 * @param showShared If files shared with the user should be listed.
	 * @return Folder contents.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo listFolder(String accountName, FileInfo folder, boolean showDeleted, boolean showShared) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		FileInfo useFolder = settings.getRootFolder();
		if (folder != null) {
			useFolder = folder;
		}
		if (useFolder.getFileType() != FileType.FOLDER) {
			throw new MultiCloudException("Supplied file instead of folder.");
		}
		synchronized (lock) {
			op = new FolderListOp(token, settings.getListDirBeginRequest(), settings.getListDirRequest(), useFolder, showDeleted);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo info = ((FolderListOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		if (info != null) {
			/* remove shared files from the result */
			if (!showShared) {
				List<FileInfo> remove = new ArrayList<>();
				for (FileInfo content: info.getContent()) {
					if (content.isShared()) {
						remove.add(content);
					}
				}
				info.getContent().removeAll(remove);
			}
			/* remove deleted files from the result */
			if (!showDeleted) {
				List<FileInfo> remove = new ArrayList<>();
				for (FileInfo content: info.getContent()) {
					if (content.isDeleted()) {
						remove.add(content);
					}
				}
				info.getContent().removeAll(remove);
			}
		}
		return info;
	}

	/**
	 * Move existing file or folder to new destination.
	 * @param accountName Name of the user account.
	 * @param file Original file or folder to be moved.
	 * @param destination Folder to move the source to.
	 * @param destinationName File or folder name in the destination location. Null to retain original.
	 * @return File or folder information after moving.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo move(String accountName, FileInfo file, FileInfo destination, String destinationName) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		if (file == null) {
			throw new MultiCloudException("File or folder must be supplied.");
		}
		if (destination == null) {
			throw new MultiCloudException("Destination folder must be supplied.");
		}
		if (destination.getFileType() != FileType.FOLDER) {
			throw new MultiCloudException("Destination must be a folder.");
		}
		synchronized (lock) {
			op = new MoveOp(token, settings.getMoveRequest(), file, destination, destinationName);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((MoveOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Runs the process for access token refreshing.
	 * Should be used only if the {@link cz.zcu.kiv.multicloud.oauth2.OAuth2Token} contains refresh token, otherwise it fails.
	 * @param accountName Name of the user account.
	 * @param callback Callback from the authorization process, if necessary.
	 * @throws MultiCloudException If some parameters were wrong.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the authorization process was interrupted.
	 */
	public void refreshAccount(String accountName, AuthorizationCallback callback) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2 auth = new OAuth2(Utils.cloudSettingsToOAuth2Settings(settings), credentialStore);
		if (callback != null) {
			auth.setAuthCallback(callback);
		}
		OAuth2Error error = auth.refresh(account.getTokenId());
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			throw new MultiCloudException("Refreshing token failed.");
		}
		accountManager.saveAccountSettings();
	}

	/**
	 * Renames the supplied file or folder.
	 * @param accountName Name of the user account.
	 * @param file File or folder to be renamed.
	 * @param fileName New file or folder name.
	 * @return File or folder information after renaming.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo rename(String accountName, FileInfo file, String fileName) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		if (file == null) {
			throw new MultiCloudException("File or folder must be supplied.");
		}
		synchronized (lock) {
			op = new RenameOp(token, settings.getRenameRequest(), file, fileName);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((RenameOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Sets the progress listener.
	 * @param listener Listener.
	 */
	public void setListener(ProgressListener listener) {
		this.listener = listener;
	}

	/**
	 * 
	 * @param accountName Name of the user account.
	 * @param destination Destination file or folder to be moved to.
	 * @param destinationName New name at the destination location.
	 * @param overwrite If the destination file should be overwritten.
	 * @param data File to be uploaded.
	 * @return File information about the uploaded file.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo uploadFile(String accountName, FileInfo destination, String destinationName, boolean overwrite, File data) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		InputStream stream = null;
		FileInfo info = null;
		try {
			stream = new FileInputStream(data);
			info = uploadFile(accountName, destination, destinationName, overwrite, stream, data.length());
		} catch (FileNotFoundException e) {
			throw new MultiCloudException("File not found.");
		} finally {
			/* close the stream when the file is uploaded */
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				/* ignore exception */
			}
		}
		return info;
	}

	/**
	 * 
	 * @param accountName Name of the user account.
	 * @param destination Destination file or folder to be moved to.
	 * @param destinationName New name at the destination location.
	 * @param overwrite If the destination file should be overwritten.
	 * @param data Data stream of the uploaded file.
	 * @param size Size of the uploaded file.
	 * @return File information about the uploaded file.
	 * @throws MultiCloudException If the operation failed.
	 * @throws OAuth2SettingsException If the authorization failed.
	 * @throws InterruptedException If the token refreshing process was interrupted.
	 */
	public FileInfo uploadFile(String accountName, FileInfo destination, String destinationName, boolean overwrite, InputStream data, long size) throws MultiCloudException, OAuth2SettingsException, InterruptedException {
		synchronized (lock) {
			if (op != null) {
				throw new MultiCloudException("Concurrent operation forbidden.");
			}
		}
		AccountSettings account = accountManager.getAccountSettings(accountName);
		if (account == null) {
			throw new MultiCloudException("User account not found.");
		}
		if (!account.isAuthorized()) {
			throw new MultiCloudException("User account not authorized.");
		}
		CloudSettings settings = cloudManager.getCloudSettings(account.getSettingsId());
		if (settings == null) {
			throw new MultiCloudException("Cloud storage settings not found.");
		}
		OAuth2Token token = credentialStore.retrieveCredential(account.getTokenId());
		if (token == null) {
			account.setTokenId(null);
			throw new MultiCloudException("Access token not found.");
		}
		if (token.isExpired()) {
			refreshAccount(accountName, null);
		}
		if (destination == null) {
			throw new MultiCloudException("Destination folder must be supplied.");
		}
		if (destination.getFileType() != FileType.FOLDER) {
			throw new MultiCloudException("Destination must be a folder.");
		}
		synchronized (lock) {
			op = new FileUploadOp(token, settings.getUploadFileBeginRequest(), settings.getUploadFileRequest(), settings.getUploadFileFinishRequest(), destination, destinationName, overwrite, data, size, listener);
		}
		op.execute();
		lastError = op.getError();
		if (op.isAborted()) {
			throw new AbortedException("Operation aborted.");
		}
		FileInfo result = ((FileUploadOp) op).getResult();
		synchronized (lock) {
			op = null;
		}
		return result;
	}

	/**
	 * Validates all user account entries and remove broken links and unused tokens.
	 */
	public void validateAccounts() {
		List<String> usedTokens = new ArrayList<>();
		/* remove broken token links */
		for (AccountSettings account: accountManager.getAllAccountSettings()) {
			if (credentialStore.retrieveCredential(account.getTokenId()) == null) {
				account.setTokenId(null);
			} else {
				usedTokens.add(account.getTokenId());
			}
		}
		accountManager.saveAccountSettings();
		/* remove unused tokens */
		for (String token: credentialStore.getIdentifiers()) {
			if (!usedTokens.contains(token)) {
				credentialStore.deleteCredential(token);
			}
		}
	}

}
