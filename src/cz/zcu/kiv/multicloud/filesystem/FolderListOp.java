package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;

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
	 * @param folder Folder to be listed.
	 */
	public FolderListOp(OAuth2Token token, CloudRequest request, FileInfo folder) {
		super(OperationType.FOLDER_LIST, token, request);
		addPropertyMapping("id", folder.getId());
		addPropertyMapping("path", folder.getPath());
		addPropertyMapping("deleted", folder.isDeleted() ? "true" : "false");
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
		System.out.println("prepared");
		try {
			setResult(executeRequest(request, new ResponseProcessor<FileInfo>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public FileInfo processResponse(HttpResponse response) {
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
					FileInfo info = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							info = json.getMapper().treeToValue(tree, FileInfo.class);
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
						e.printStackTrace(); // debug
					}
					return info;
				}
			}));
		} catch (IOException e) {
			throw new MultiCloudException("Failed to list the selected folder.");
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
