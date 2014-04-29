package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadOp.java
 *
 * Operation for downloading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadOp extends Operation<FileInfo> {

	/** Size of a chunk for file download. */
	public static final long CHUNK_SIZE = 4 *1024 * 1024;

	public FileDownloadOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.FILE_DOWNLOAD, token, request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

}
