package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.http.HttpCopy;
import cz.zcu.kiv.multicloud.http.HttpMove;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.HttpMethod;
import cz.zcu.kiv.multicloud.utils.Utils;

public abstract class Operation<T> {

	private final OperationType type;
	private final OAuth2Token token;
	private final Map<String, String> propertyMapping;
	private String authorizationParam;
	private T result;

	protected final Json json;

	protected HttpMethod method;
	protected String uriTemplate;
	protected Map<String, String> jsonMapping;
	protected Map<String, String> requestHeaders;
	protected Map<String, String> requestParams;
	protected Map<String, String> responseHeaders;
	protected Map<String, String> responseParams;

	public Operation(OperationType type, OAuth2Token token, CloudRequest request) {
		this.type = type;
		this.token = token;
		this.propertyMapping = new HashMap<>();
		this.authorizationParam = null;
		this.result = null;

		json = Json.getInstance();

		method = request.getMethod();
		uriTemplate = request.getUri();
		jsonMapping = request.getMapping();
		requestHeaders = request.getHeaders();
		if (requestHeaders == null) {
			requestHeaders = new HashMap<>();
		}
		requestParams = request.getParams();
		if (requestParams == null) {
			requestParams = new HashMap<>();
		}
		if (!Utils.isNullOrEmpty(request.getAuthorizationParam())) {
			enableAuthorizationParam(request.getAuthorizationParam());
		} else {
			enableAuthorizationHeader();
		}
		responseHeaders = new HashMap<>();
		responseParams = new HashMap<>();
	}

	protected void addPropertyMapping(String property, String data) {
		propertyMapping.put(property, data);
	}

	protected void disableAuthorizationHeader() {
		requestHeaders.remove("Authorization");
	}

	protected void disableAuthorizationParam() {
		requestParams.remove(authorizationParam);
		authorizationParam = null;
	}

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
			Matcher matcher = pattern.matcher(mapping.getValue());
			if (!matcher.find()) {
				result = result.replaceAll(find, mapping.getValue());
			}
		}
		return result;
	}

	protected void enableAuthorizationHeader() {
		requestHeaders.put("Authorization", token.toHeaderString());
	}

	protected void enableAuthorizationParam(String param) {
		authorizationParam = param;
		requestParams.put(param, token.getAccessToken());
	}

	public void execute() throws MultiCloudException {
		operationBegin();
		operationExecute();
		operationFinish();
	}

	protected void executeRequest(HttpUriRequest request, ResponseProcessor<T> processor) throws IOException {
		/* clear the response parameters */
		responseHeaders.clear();
		responseParams.clear();
		/* send the request and process the response */
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(request);
		for (Header header: response.getAllHeaders()) {
			responseHeaders.put(header.getName(), header.getValue());
		}
		result = processor.processResponse(response);
		response.close();
		client.close();
	}

	public T getResult() {
		return result;
	}

	public OperationType getType() {
		return type;
	}

	protected boolean isAuthorizationHeaderEnabled() {
		return (requestHeaders.containsKey("Authorization"));
	}

	protected boolean isAuthorizationParamEnabled() {
		return (authorizationParam != null);
	}

	protected abstract void operationBegin() throws MultiCloudException;

	protected abstract void operationExecute() throws MultiCloudException;

	protected abstract void operationFinish() throws MultiCloudException;

	protected HttpUriRequest prepareRequest(HttpEntity requestData) {
		HttpUriRequest request = null;
		String uri = doPropertyMapping(uriTemplate);
		if (!requestParams.isEmpty()) {
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
			request.addHeader(header.getKey(), header.getValue());
		}
		if (request instanceof HttpEntityEnclosingRequestBase) {
			((HttpEntityEnclosingRequestBase) request).setEntity(requestData);
		}
		return request;
	}

}
