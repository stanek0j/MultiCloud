package cz.zcu.kiv.multicloud.filesystem;

import java.util.List;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

public class SearchOp extends Operation<List<FileInfo>> {

	public SearchOp(OAuth2Token token, CloudRequest request, FileInfo folder, String fileName, boolean showDeleted) {
		super(OperationType.SEARCH, token, request);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void abort() {
		// TODO Auto-generated method stub

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
