package cz.zcu.kiv.multicloud.core.oauth2;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.http.Status;

public abstract class WebPage {
	
	protected Status status;
	protected Map<String, String> headers;
	
	public WebPage() {
		status = Status.OK;
		headers = new HashMap<>();
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public void setStatus(int code) {
		status = Status.getStatus(code);
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public void setHeaders(Map<String, String> headers) {
		if (headers != null) {
			this.headers = headers;
		}
	}
	
	public void setHeader(String header, String value) {
		headers.put(header, value);
	}
	
	public abstract String getContent();

}
