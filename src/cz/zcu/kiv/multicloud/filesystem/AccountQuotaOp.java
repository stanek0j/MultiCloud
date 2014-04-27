package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.AccountQuota;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/AccountQuotaOp.java
 *
 * Operation for getting information about the user quota.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountQuotaOp extends Operation<AccountQuota> {

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 */
	public AccountQuotaOp(OAuth2Token token, CloudRequest request) {
		super(OperationType.ACCOUNT_QUOTA, token, request);
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
		HttpUriRequest request = prepareRequest(null);
		try {
			setResult(executeRequest(request, new ResponseProcessor<AccountQuota>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public AccountQuota processResponse(HttpResponse response) {
					AccountQuota quota = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							quota = json.getMapper().treeToValue(tree, AccountQuota.class);
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
					}
					return quota;
				}
			}));
		} catch (IOException e) {
			throw new MultiCloudException("Failed to get user quota information.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() throws MultiCloudException {
		/* no finalization necessary */
	}

}