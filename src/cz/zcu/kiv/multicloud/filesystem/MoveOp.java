package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

public class MoveOp extends Operation {

	public MoveOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.MOVE, token, request);
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