package cz.zcu.kiv.multicloud.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.UploadSession;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileUploadOp.java
 *
 * Operation for uploading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileUploadOp extends Operation<FileInfo> {

	/** Size of a chunk for file download. Default value is set to 4 MiB. */
	public static final long CHUNK_SIZE = 4 * 1024 * 1024;
	/** String to indicate that the body of the request should contain upload data. */
	public static final String DATA_MAPPING = "<data>";

	/** File or folder name to be renamed to. */
	private final String name;
	/** JSON body represented as a map. */
	private Map<String, Object> jsonBody;
	/** Body of the request. */
	private String body;
	/** Parameters of the initial request. */
	private final CloudRequest beginRequest;
	/** Parameters of the chunked upload request. */
	private final CloudRequest execRequest;
	/** Parameters if the finalization request. */
	private final CloudRequest finishRequest;
	/** Size of the data uploaded. */
	private final long size;
	/** File data to be uploaded. */
	private final InputStream data;

	/** Number of bytes already sent to the server. */
	private long transferred;
	/** Identifier of the chunked upload session. */
	private UploadSession session;
	/** Byte buffer of the last chunk. */
	private byte[] buffer;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param beginRequest Parameters of the request to start the upload.
	 * @param execRequest Parameters of the request to upload the data.
	 * @param finishRequest Parameters of the request to finish the upload.
	 * @param destination Destination file or folder to be moved to.
	 * @param destinationName New name at the destination location.
	 * @param overwrite If the destination file should be overwritten.
	 * @param data Data stream of the uploaded file.
	 * @param size Size of the uploaded file.
	 */
	public FileUploadOp(
			OAuth2Token token,
			CloudRequest beginRequest,
			CloudRequest execRequest,
			CloudRequest finishRequest,
			FileInfo destination,
			String destinationName,
			boolean overwrite,
			InputStream data,
			long size) {
		super(OperationType.FILE_UPLOAD, token, beginRequest);
		this.beginRequest = beginRequest;
		this.execRequest = execRequest;
		this.finishRequest = finishRequest;
		this.size = size;
		this.data = data;

		addPropertyMapping("id", destination.getId());
		addPropertyMapping("destination_id", destination.getId());
		String path = destination.getPath();
		if (path != null) {
			if (path.endsWith(FileInfo.PATH_SEPARATOR)) {
				if (destinationName != null) {
					path += destinationName;
				}
			} else {
				if (destinationName != null) {
					path += FileInfo.PATH_SEPARATOR + destinationName;
				}
			}
		}
		addPropertyMapping("path", path);
		addPropertyMapping("destination_path", path);
		if (destinationName != null) {
			addPropertyMapping("name", destinationName);
			name = destinationName;
		} else {
			name = null;
		}
		addPropertyMapping("overwrite", overwrite ? "true" : "false");
		addPropertyMapping("size", String.valueOf(size));
		transferred = 0;
	}

	/**
	 * Beginning of the chunked upload. If the request parameters for this method are supplied, chunked upload is started.
	 * The main purpose of this method is to obtain a session identifier for further data upload.
	 * This request uploads maximum one single chunk of data.
	 */
	@Override
	protected void operationBegin() throws MultiCloudException {
		if (beginRequest != null) {
			setRequest(beginRequest);
			jsonBody = beginRequest.getJsonBody();
			body = beginRequest.getBody();
			addPropertyMapping("offset", String.valueOf(transferred));
			HttpUriRequest request = prepareRequest(null);
			try {
				if (jsonBody != null) {
					ObjectMapper mapper = json.getMapper();
					body = mapper.writeValueAsString(jsonBody);
					request = prepareRequest(new StringEntity(doPropertyMapping(body)));
				} else {
					if (body != null) {
						if (body.equals(DATA_MAPPING)) {
							ByteArrayInputStream data = readData();
							transferred = buffer.length;
							request = prepareRequest(new InputStreamEntity(data, buffer.length));
						} else {
							request = prepareRequest(new StringEntity(doPropertyMapping(body)));
						}
					} else {
						request = prepareRequest(null);
					}
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
						session = null;
						try {
							if (response.getStatusLine().getStatusCode() >= 400) {
								parseOperationError(response);
							} else {
								JsonNode tree = parseJsonResponse(response);
								if (tree != null) {
									session = json.getMapper().treeToValue(tree, UploadSession.class);
								} else {
									for (Entry<String, String> header: responseHeaders.entrySet()) {
										if (header.getKey().equals("Location")) {
											responseParams.putAll(Utils.extractParams(header.getValue()));
											doResponseParamsMapping();
											session = new UploadSession();
											session.setSession(responseParams.get("session"));
											try {
												long offset = Long.parseLong(responseParams.get("offset"));
												session.setOffset(offset);
											} catch (NumberFormatException e) {
												session.setOffset(0);
											}
										}
									}
								}
							}
						} catch (IllegalStateException | IOException e) {
							/* return null value instead of throwing exception */
						}
						return null;
					}
				}));
			} catch (IOException e) {
				throw new MultiCloudException("Failed to upload the file.");
			}
		}
	}

	/**
	 * Chunked upload progress. Uploads all the remaining data chunks.
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		if (execRequest != null) {
			if (session != null) {
				addPropertyMapping("session", session.getSession());
			}

			while (transferred < size) {
				setRequest(execRequest);
				jsonBody = execRequest.getJsonBody();
				body = execRequest.getBody();
				addPropertyMapping("offset", String.valueOf(transferred));
				HttpUriRequest request = prepareRequest(null);
				try {
					if (jsonBody != null) {
						ObjectMapper mapper = json.getMapper();
						body = mapper.writeValueAsString(jsonBody);
						request = prepareRequest(new StringEntity(doPropertyMapping(body)));
					} else {
						if (body != null) {
							if (body.equals(DATA_MAPPING)) {
								ByteArrayInputStream data = readData();
								transferred += buffer.length;
								addPropertyMapping("offsetbuffer", String.valueOf(transferred - 1));
								request = prepareRequest(new InputStreamEntity(data, buffer.length));
							} else {
								request = prepareRequest(new StringEntity(doPropertyMapping(body)));
							}
						} else {
							request = prepareRequest(null);
						}
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
							try {
								if (response.getStatusLine().getStatusCode() >= 400) {
									parseOperationError(response);
								}
							} catch (IllegalStateException | IOException e) {
								/* return null value instead of throwing exception */
							}
							return null;
						}
					}));
				} catch (IOException e) {
					throw new MultiCloudException("Failed to upload the file.");
				}
			}
		}
	}

	/**
	 * Finish the upload of the file. If no data were submitted so far, a normal direct upload is performed.
	 */
	@Override
	protected void operationFinish() throws MultiCloudException {
		if (finishRequest != null) {
			setRequest(finishRequest);
			jsonBody = finishRequest.getJsonBody();
			body = finishRequest.getBody();
			if (session != null) {
				addPropertyMapping("session", session.getSession());
			}
			addPropertyMapping("offset", String.valueOf(transferred));
			HttpUriRequest request = prepareRequest(null);
			try {
				if (jsonBody != null) {
					ObjectMapper mapper = json.getMapper();
					body = mapper.writeValueAsString(jsonBody);
					request = prepareRequest(new StringEntity(doPropertyMapping(body)));
				} else {
					if (body != null) {
						if (body.equals(DATA_MAPPING)) {
							transferred = size;
							request = prepareRequest(new InputStreamEntity(data, size));
						} else {
							request = prepareRequest(new StringEntity(doPropertyMapping(body)));
						}
					} else {
						request = prepareRequest(null);
					}
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
									info.setFileType(FileType.FILE);
								}
							}
						} catch (IllegalStateException | IOException e) {
							/* return null value instead of throwing exception */
						}
						return info;
					}
				}));
			} catch (IOException e) {
				throw new MultiCloudException("Failed to upload the file.");
			}
		} else {
			if (getError() == null && getResult() == null) {
				FileInfo info = new FileInfo();
				info.setName(name);
				info.setFileType(FileType.FILE);
				setResult(info);
			}
		}
	}

	/**
	 * Read chunk from the input stream, save it to a buffer and return input stream made off that buffer.
	 * @return Chunk data stream.
	 */
	private ByteArrayInputStream readData() {
		long size = CHUNK_SIZE;
		if (this.size - transferred < size) {
			size = this.size - transferred;
		}
		buffer = new byte[(int) size];
		try {
			data.read(buffer, 0, (int) size);
		} catch (IOException e) {
			/* returns empty buffer */
		}
		return new ByteArrayInputStream(buffer);
	}

}
