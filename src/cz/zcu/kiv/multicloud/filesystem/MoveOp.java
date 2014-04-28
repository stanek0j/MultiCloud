package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/MoveOp.java
 *
 * Operation for moving a file or folder to the specified location, including renaming.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MoveOp extends Operation<FileInfo> {

	private final Map<String, Object> jsonBody;
	private final String body;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 * @param source Source file or folder to be moved.
	 * @param destination Destination file or folder to be moved to.
	 * @param destinationName New name at the destination location.
	 */
	public MoveOp(OAuth2Token token, CloudRequest request, FileInfo source, FileInfo destination, String destinationName) {
		super(OperationType.MOVE, token, request);
		addPropertyMapping("id", source.getId());
		addPropertyMapping("path", source.getPath());
		addPropertyMapping("source_id", source.getId());
		addPropertyMapping("source_path", source.getPath());
		addPropertyMapping("destination_id", destination.getId());
		String path = destination.getPath();
		if (path != null) {
			if (path.endsWith(FileInfo.PATH_SEPARATOR)) {
				if (destinationName != null) {
					path += destinationName;
				} else {
					path += source.getName();
				}
			} else {
				if (destinationName != null) {
					path += FileInfo.PATH_SEPARATOR + destinationName;
				} else {
					path += FileInfo.PATH_SEPARATOR + source.getName();
				}
			}
		}
		addPropertyMapping("destination_path", path);
		jsonBody = request.getJsonBody();
		body = request.getBody();
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
			setResult(executeRequest(request, new ResponseProcessor<FileInfo>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public FileInfo processResponse(HttpResponse response) {
					FileInfo info = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							if (tree != null) {
								info = json.getMapper().treeToValue(tree, FileInfo.class);
								info.fillMissing();
								for (FileInfo content: info.getContent()) {
									content.fillMissing();
								}
							}
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
					}
					return info;
				}
			}));
		} catch (IOException e) {
			throw new MultiCloudException("Failed to move the specified file or folder to the destination.");
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
