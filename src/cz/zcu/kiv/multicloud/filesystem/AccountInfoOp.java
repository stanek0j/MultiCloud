package cz.zcu.kiv.multicloud.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

public class AccountInfoOp extends Operation<AccountInfo> {

	public AccountInfoOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.ACCOUNT_INFO, token, request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() {
		/* no preparation necessary */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		HttpUriRequest request = prepareRequest(null);
		try {
			executeRequest(request, new ResponseProcessor<AccountInfo>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public AccountInfo processResponse(HttpResponse response) {
					try {
						BufferedReader bfr = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String line = null;
						while ((line = bfr.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IllegalStateException | IOException e) {
						e.printStackTrace();
					}
					AccountInfo info = new AccountInfo();
					info.setId("test");
					info.setName("test");
					return info;
				}
			});
		} catch (IOException e) {
			throw new MultiCloudException("Failed to get user account information.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() {
		/* no finalization necessary */
	}

}
