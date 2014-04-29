package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.http.HttpCopy;
import cz.zcu.kiv.multicloud.http.HttpMove;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.json.OperationError;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.HttpMethod;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.filesystem/Operation.java
 *
 * Generic template for implementing any operation with the user account storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 * @param <T>
 */
public abstract class Operation<T> {

	/** Separator for multiple JSON value mapped into one. */
	public static final String JSON_MAPPING_SEPARATOR = ";";
	/** Separator for selecting path in the JSON response. */
	public static final String JSON_PATH_SEPARATOR = "/";

	/** Type of operation. */
	private final OperationType type;
	/** Access token for the user account storage service. */
	private final OAuth2Token token;
	/** Mapping of non-generic string values. */
	private final Map<String, String> propertyMapping;
	/** Name of the URI parameter containing access token. */
	private String authorizationParam;
	/** Result of the operation. */
	private T result;
	/** Error that occurred during the operation. */
	private OperationError error;

	/** JSON factory and Object mapper. */
	protected final Json json;

	/** HTTP method used by the request. */
	protected HttpMethod method;
	/** URI template of the request. */
	protected String uriTemplate;
	/** Headers of the request. */
	protected Map<String, String> requestHeaders;
	/** Parameters of the request. */
	protected Map<String, String> requestParams;
	/** Headers of the response. */
	protected Map<String, String> responseHeaders;
	/** Parameters of the response. */
	protected Map<String, String> responseParams;
	/** Mapping of the values in the response. */
	protected Map<String, String> responseMapping;

	/**
	 * Ctor with necessary parameters.
	 * @param type Type of the operation.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 */
	public Operation(OperationType type, OAuth2Token token, CloudRequest request) {
		this.type = type;
		this.token = token;
		this.propertyMapping = new HashMap<>();
		this.authorizationParam = null;
		this.result = null;
		this.error = null;

		json = Json.getInstance();

		setRequest(request);
		responseHeaders = new HashMap<>();
		responseParams = new HashMap<>();
	}

	/**
	 * Adds new mapping of non-generic string values.
	 * @param property Property to be replaced.
	 * @param data Data to replace it with.
	 */
	protected void addPropertyMapping(String property, String data) {
		propertyMapping.put(property, data);
	}

	/**
	 * Removes the header for holding access token from the request.
	 */
	protected void disableAuthorizationHeader() {
		requestHeaders.remove("Authorization");
	}

	/**
	 * Removes the parameter for holding access token from the request.
	 */
	protected void disableAuthorizationParam() {
		requestParams.remove(authorizationParam);
		authorizationParam = null;
	}

	/**
	 * Recursive JSON value mapping.
	 * @param root Relative root of the tree.
	 * @return Tree with mapped values.
	 */
	private JsonNode doJsonMapping(JsonNode root) {
		for (Entry<String, String> mapping: responseMapping.entrySet()) {
			for (String submapping: mapping.getValue().split(JSON_MAPPING_SEPARATOR)) {
				JsonNode node = root;
				for (String subpath: submapping.split(JSON_PATH_SEPARATOR)) {
					node = node.path(subpath);
				}
				JsonNode existing = root.path(mapping.getKey());
				if (!node.isMissingNode()) {
					switch (node.getNodeType()) {
					case STRING:
						String text = "";
						if (!existing.isMissingNode() && existing.isTextual()) {
							text += existing.textValue(); // prepend existing string
						}
						text += node.textValue();
						((ObjectNode) root).put(mapping.getKey(), text);
						break;
					case NUMBER:
						switch (node.numberType()) {
						case INT:
						case LONG:
						case BIG_INTEGER:
							long numLng = node.longValue();
							if (!existing.isMissingNode() && existing.isNumber()) {
								switch (existing.numberType()) {
								case INT:
								case LONG:
								case BIG_INTEGER:
									numLng += existing.longValue(); // add numbers
									((ObjectNode) root).put(mapping.getKey(), numLng);
									break;
								case FLOAT:
								case DOUBLE:
								case BIG_DECIMAL:
									double exDbl = existing.doubleValue() + numLng; // add numbers
									((ObjectNode) root).put(mapping.getKey(), exDbl);
									break;
								}
							} else {
								((ObjectNode) root).put(mapping.getKey(), numLng);
							}
							break;
						case FLOAT:
						case DOUBLE:
						case BIG_DECIMAL:
							double numDbl = node.doubleValue();
							if (!existing.isMissingNode() && existing.isNumber()) {
								numDbl += existing.doubleValue(); // add numbers
								((ObjectNode) root).put(mapping.getKey(), numDbl);
							}
							break;
						}
						break;
					case OBJECT:
						JsonNode mappedObjectNode = doJsonMapping(node); // map JSON values
						((ObjectNode) root).put(mapping.getKey(), mappedObjectNode);
						break;
					case ARRAY:
						if (!existing.isMissingNode() && existing.isArray()) {
							((ArrayNode) node).addAll((ArrayNode) existing); // append array values
						}
						JsonNode mappedArrayNode = doJsonMapping(node); // map JSON values
						ArrayNode deeplyMappedArrayNode = new ArrayNode(json.getMapper().getNodeFactory());
						Iterator<JsonNode> it = ((ArrayNode) mappedArrayNode).elements();
						while (it.hasNext()) {
							JsonNode element = it.next();
							deeplyMappedArrayNode.add(doJsonMapping(element)); // map values inside the array
						}
						((ObjectNode) root).put(mapping.getKey(), deeplyMappedArrayNode);
						break;
					case BOOLEAN:
						((ObjectNode) root).put(mapping.getKey(), node.booleanValue());
						break;
					case NULL:
					default:
						break;
					}
				}
			}
		}
		return root;
	}

	/**
	 * Finds all non-generic strings and replaces them with corresponding values.
	 * @param source The string to replace these properties in.
	 * @return String with replaces values.
	 */
	protected String doPropertyMapping(String source) {
		String result = source;
		Pattern pattern = Pattern.compile("(<.*?>)");
		for (Entry<String, String> mapping: propertyMapping.entrySet()) {
			String find = mapping.getKey();
			if (!find.startsWith("<")) {
				find = "<" + find;
			}
			if (!find.endsWith(">")) {
				find += ">";
			}
			Matcher matcher = pattern.matcher(result);
			if (matcher.find()) {
				result = result.replaceAll(find, mapping.getValue());
			}
		}
		return result;
	}

	/**
	 * Mapping of the response parameters.
	 */
	protected void doResponseParamsMapping() {
		Map<String, String> add = new HashMap<>();
		List<String> remove = new ArrayList<>();
		for (Entry<String, String> param: responseParams.entrySet()) {
			for (Entry<String, String> mapping: responseMapping.entrySet()) {
				if (param.getKey().equals(mapping.getValue())) {
					add.put(mapping.getKey(), param.getValue());
					if (!param.getKey().equals(mapping.getKey())) {
						remove.add(param.getKey());
					}
					break;
				}
			}
		}
		responseParams.putAll(add);
		for (String key: remove) {
			responseParams.remove(key);
		}
	}

	/**
	 * Adds the header for holding access token in the request.
	 */
	protected void enableAuthorizationHeader() {
		requestHeaders.put("Authorization", token.toHeaderString());
	}

	/**
	 * Adds the parameter for holding access token in the requst.
	 * @param param Parameter name.
	 */
	protected void enableAuthorizationParam(String param) {
		authorizationParam = param;
		requestParams.put(param, token.getAccessToken());
	}

	/**
	 * Executes the operation.
	 * @throws MultiCloudException If the operation fails for some reason.
	 */
	public void execute() throws MultiCloudException {
		operationBegin();
		operationExecute();
		operationFinish();
	}

	/**
	 * Executes the prepared {@link org.apache.http.client.methods.HttpUriRequest} and handles the response in provided {@link cz.zcu.kiv.multicloud.filesystem.ResponseProcessor}.
	 * @param request Prepared HTTP request.
	 * @param processor Response processor.
	 * @throws IOException If something fails.
	 */
	protected T executeRequest(HttpUriRequest request, ResponseProcessor<T> processor) throws IOException {
		/* clear the response parameters */
		responseHeaders.clear();
		responseParams.clear();
		/* send the request and process the response */
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(request);
		System.out.println(request.getRequestLine());
		for (Header header: response.getAllHeaders()) {
			responseHeaders.put(header.getName(), header.getValue());
		}
		T result = processor.processResponse(response);
		response.close();
		client.close();
		return result;
	}

	/**
	 * Returns the error that occurred during the operation.
	 * @return Error occurred.
	 */
	public OperationError getError() {
		return error;
	}

	/**
	 * Returns the result of the operation. Returns null if the operation fails.
	 * @return Result of the operation.
	 */
	public T getResult() {
		return result;
	}

	/**
	 * Returns the type of the operation.
	 * @return Operation type.
	 */
	public OperationType getType() {
		return type;
	}

	/**
	 * Determines if the header for holding access token is enabled.
	 * @return If the header is enabled.
	 */
	protected boolean isAuthorizationHeaderEnabled() {
		return (requestHeaders.containsKey("Authorization"));
	}

	/**
	 * Determines if the parameter for holding access token is enabled.
	 * @return If the parameter is enabled.
	 */
	protected boolean isAuthorizationParamEnabled() {
		return (authorizationParam != null);
	}

	/**
	 * Method to be executed at the beginning of the operation execution.
	 * @throws MultiCloudException If something failed.
	 */
	protected abstract void operationBegin() throws MultiCloudException;

	/**
	 * Main method of the execution of the operation. Supposed to do all the work.
	 * @throws MultiCloudException If something failed.
	 */
	protected abstract void operationExecute() throws MultiCloudException;

	/**
	 * Method to be executed at the end of the operation execution.
	 * @throws MultiCloudException If something failed.
	 */
	protected abstract void operationFinish() throws MultiCloudException;

	/**
	 * Parses the {@link org.apache.http.HttpResponse} as a JSON string and returns the root of the tree. Also performs the response JSON values mapping.
	 * @param response Response to be parsed.
	 * @return Root of the parsed tree. Null on failed parsing.
	 */
	protected JsonNode parseJsonResponse(HttpResponse response) {
		if (response.getEntity() != null) {
			ObjectMapper mapper = json.getMapper();
			try {
				JsonNode root = mapper.readTree(response.getEntity().getContent());
				/* mapping JSON values to different field names */
				root = doJsonMapping(root);
				return root;
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Parses the {@link org.apache.http.HttpResponse} as a JSON string and saves the error message it contains.
	 * @param response Response to be parsed.
	 * @throws IOException If something failed.
	 */
	protected void parseOperationError(HttpResponse response) throws IOException {
		ObjectMapper mapper = json.getMapper();
		JsonNode root = mapper.readTree(response.getEntity().getContent());
		JsonNode node = root.path("error");
		if (node.isObject()) {
			error = mapper.treeToValue(node, OperationError.class);
			if (error.getCode() == -1) {
				error.setCode(response.getStatusLine().getStatusCode());
			}
		} else if (!node.isMissingNode()) {
			error = new OperationError();
			error.setCode(response.getStatusLine().getStatusCode());
			error.setMessage(node.textValue());
		}
	}

	/**
	 * Prepares {@link org.apache.http.client.methods.HttpUriRequest} and fills it with data provided.
	 * @param requestData Data for filling the body of the request.
	 * @return Prepared request.
	 */
	protected HttpUriRequest prepareRequest(HttpEntity requestData) {
		HttpUriRequest request = null;
		String uri = doPropertyMapping(uriTemplate);
		if (!requestParams.isEmpty()) {
			for (Entry<String, String> param: requestParams.entrySet()) {
				String update = doPropertyMapping(param.getValue());
				requestParams.put(param.getKey(), update);
			}
			uri += "?" + URLEncodedUtils.format(Utils.mapToList(requestParams), Charset.forName("utf-8"));
		}
		switch (method) {
		case GET:
			request = new HttpGet(uri);
			break;
		case POST:
			request = new HttpPost(uri);
			break;
		case PUT:
			request = new HttpPut(uri);
			break;
		case DELETE:
			request = new HttpDelete(uri);
			break;
		case COPY:
			request = new HttpCopy(uri);
			break;
		case MOVE:
			request = new HttpMove(uri);
			break;
		case HEAD:
			request = new HttpHead(uri);
			break;
		case OPTIONS:
			request = new HttpOptions(uri);
			break;
		case PATCH:
			request = new HttpPatch(uri);
			break;
		case TRACE:
			request = new HttpTrace(uri);
			break;
		}
		for (Entry<String, String> header: requestHeaders.entrySet()) {
			request.addHeader(header.getKey(), doPropertyMapping(header.getValue()));
		}
		if (request instanceof HttpEntityEnclosingRequestBase) {
			((HttpEntityEnclosingRequestBase) request).setEntity(requestData);
		}
		return request;
	}

	/**
	 * Sets the parameters of the request.
	 * @param request Parameters of the request.
	 */
	protected void setRequest(CloudRequest request) {
		if (request != null) {
			method = request.getMethod();
			uriTemplate = request.getUri();
			requestHeaders = new HashMap<>();
			if (request.getHeaders() != null) {
				requestHeaders.putAll(request.getHeaders());
			}
			requestParams = new HashMap<>();
			if (request.getParams() != null) {
				requestParams.putAll(request.getParams());
			}
			responseMapping = new HashMap<>();
			if (request.getMapping() != null) {
				responseMapping.putAll(request.getMapping());
			}
			if (!Utils.isNullOrEmpty(request.getAuthorizationParam())) {
				enableAuthorizationParam(request.getAuthorizationParam());
			} else {
				enableAuthorizationHeader();
			}
		} else {
			method = HttpMethod.GET;
			uriTemplate = null;
			responseMapping = new HashMap<>();
			requestHeaders = new HashMap<>();
			requestParams = new HashMap<>();
		}
	}

	/**
	 * Sets the result of the operation.
	 * @param result Result of the operation.
	 */
	protected void setResult(T result) {
		this.result = result;
	}

}
