package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/FolderListOp.java
 *
 * Operation for getting the list of folders and files inside the specified folder of the storage space.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FolderListOp extends Operation<FileInfo> {

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 */
	public FolderListOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.FOLDER_LIST, token, request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() throws MultiCloudException {
		/* no preparation necessary */
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
		/* no finalization necessary */
	}

}
