package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.AccountInfo;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/AccountInfoOp.java
 *
 * Operation for getting the user name and user identifier for the user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountInfoOp extends Operation<AccountInfo> {

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 */
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
			setResult(executeRequest(request, new ResponseProcessor<AccountInfo>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public AccountInfo processResponse(HttpResponse response) {
					/*
					try {
						BufferedReader bfr = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String line = null;
						while ((line = bfr.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IllegalStateException | IOException e1) {
						e1.printStackTrace();
					}
					System.out.println("Code: " + response.getStatusLine().getStatusCode());
					for (Entry<String, String> header: responseHeaders.entrySet()) {
						System.out.println(header.getKey() + ": " + header.getValue());
					}
					 */
					AccountInfo info = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							info = json.getMapper().treeToValue(tree, AccountInfo.class);
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
						e.printStackTrace();
					}
					return info;
				}
			}));
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
