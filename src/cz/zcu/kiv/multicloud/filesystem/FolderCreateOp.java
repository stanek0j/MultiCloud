package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.filesystem/FolderCreateOp.java
 *
 * Operation for creating a new folder in the specified location of the storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FolderCreateOp extends Operation<FileInfo> {

	private final Map<String, Object> jsonBody;
	private String body;
	private final String name;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 * @param folderName Name of the created folder.
	 * @param parent Parent folder.
	 */
	public FolderCreateOp(OAuth2Token token, CloudRequest request, String folderName, FileInfo parent) {
		super(OperationType.FOLDER_CREATE, token, request);
		name = folderName;
		addPropertyMapping("name", folderName);
		addPropertyMapping("id", parent.getId());
		String path = parent.getPath();
		if (path != null) {
			if (path.endsWith(FileInfo.PATH_SEPARATOR)) {
				path += folderName;
			} else {
				path += FileInfo.PATH_SEPARATOR + folderName;
			}
		}
		addPropertyMapping("path", path);
		jsonBody = request.getJsonBody();
		body = request.getBody();
		if (jsonBody != null) {
			/* remove parent entries from JSON body of the request, if there are no parents set */
			if (Utils.isNullOrEmpty(parent.getId()) && Utils.isNullOrEmpty(parent.getPath())) {
				String parentKey = "parents";
				/* seek property mapping name */
				if (request.getMapping() != null) {
					for (Entry<String, String> mapping: request.getMapping().entrySet()) {
						if (mapping.getKey().equals(parentKey)) {
							parentKey = mapping.getValue();
							break;
						}
					}
				}
				jsonBody.remove(parentKey);
			}
		}
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
		HttpUriRequest request = null;
		try {
			if (jsonBody != null) {
				ObjectMapper mapper = json.getMapper();
				body = mapper.writeValueAsString(jsonBody);
			}
			if (body != null) {
				request = prepareRequest(new StringEntity(doPropertyMapping(body)));
			} else {
				request = prepareRequest(null);
			}
		} catch (UnsupportedEncodingException | JsonProcessingException e1) {
			throw new MultiCloudException("Failed to prepare request.");
		}
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
							} else {
								info = new FileInfo();
								info.setName(name);
								info.setFileType(FileType.FOLDER);
							}
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
					}
					return info;
				}
			}));
		} catch (IOException e) {
			throw new MultiCloudException("Failed to create the folder.");
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
