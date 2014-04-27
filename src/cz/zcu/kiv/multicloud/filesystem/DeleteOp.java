package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

public class DeleteOp extends Operation {

	public DeleteOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.DELETE, token, request);
	}

	@Override
	protected void operationBegin() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void operationExecute() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void operationFinish() throws MultiCloudException {
		// TODO Auto-generated method stub

	}

}
