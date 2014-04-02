package cz.zcu.kiv.multicloud.core.oauth2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import cz.zcu.kiv.multicloud.core.Utils;
import cz.zcu.kiv.multicloud.core.json.Json;

public class AuthorizationCodeGrant implements OAuth2Grant, RedirectCallback {

	private final Json json;

	private final Thread tokenRequest;
	private final RedirectServer server;
	private String state;

	protected final OAuth2Token token;
	protected final OAuth2Error error;
	protected boolean ready;
	protected Object waitObject;

	protected String authorizeServer;
	protected String tokenServer;
	protected Map<String, Object> authorizeParams;
	protected Map<String, Object> tokenParams;

	/**
	 * Ctor.
	 */
	public AuthorizationCodeGrant() {
		json = Json.getInstance();
		token = new OAuth2Token();
		error = new OAuth2Error();
		server = new RedirectServer();
		server.setRedirectCallback(this);
		authorizeParams = new HashMap<>();
		tokenParams = new HashMap<>();
		tokenRequest = new Thread() {
			/**
			 * Obtain access (and refresh) token from the authorization server.
			 */
			@Override
			public void run() {
				obtainAccessToken();
			}
		};
		ready = false;
		waitObject = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationRequest authorize() {
		String queryString = URLEncodedUtils.format(Utils.mapToList(authorizeParams), Charset.forName("utf-8"));
		return new AuthorizationRequest(authorizeServer + "?" + queryString);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OAuth2Error getError() {
		try {
			synchronized (waitObject) {
				while (!ready) {
					waitObject.wait();
				}
			}
			tokenRequest.join();
		} catch (InterruptedException e) {
			return null;
		}
		return error;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OAuth2Token getToken() {
		try {
			synchronized (waitObject) {
				while (!ready) {
					waitObject.wait();
				}
			}
			tokenRequest.join();
		} catch (InterruptedException e) {
			return null;
		}
		return token;
	}

	/**
	 * Sends a POST request to obtain an access token.
	 */
	private void obtainAccessToken() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost request = new HttpPost(tokenServer);
			request.setEntity(new UrlEncodedFormEntity(Utils.mapToList(tokenParams)));
			CloseableHttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			JsonParser jp = json.getFactory().createParser(entity.getContent());
			while (jp.nextToken() != null) {
				JsonToken jsonToken = jp.getCurrentToken();
				switch (jsonToken) {
				case FIELD_NAME:
					String name = jp.getCurrentName();
					jsonToken = jp.nextToken();
					if (name.equals("access_token")) {
						token.setAccessToken(jp.getValueAsString());
					} else if (name.equals("token_type")) {
						token.setType(OAuth2TokenType.valueOf(jp.getValueAsString().toUpperCase()));
					} else if (name.equals("expires_in")) {
						token.setExpiresIn(jp.getValueAsInt());
					} else if (name.equals("refresh_token")) {
						token.setRefreshToken(jp.getValueAsString());
					} else if (name.equals("kid")) {
						token.setKeyId(jp.getValueAsString());
					} else if (name.equals("mac_key")) {
						token.setMacKey(jp.getValueAsString());
					} else if (name.equals("mac_algorithm")) {
						token.setMacAlgorithm(jp.getValueAsString());
					} else if (name.equals("error")) {
						error.setType(OAuth2ErrorType.valueOf(jp.getValueAsString().toUpperCase()));
					} else if (name.equals("error_description")) {
						error.setDescription(jp.getValueAsString());
					} else if (name.equals("error_uri")) {
						error.setUri(jp.getValueAsString());
					}
					ready = true;
					break;
				default:
					break;
				}
			}
			jp.close();
			response.close();
			client.close();
			synchronized (waitObject) {
				ready = true;
				waitObject.notifyAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebPage onRedirect(Map<String, String> request) {
		RedirectWebPage page = new RedirectWebPage();
		page.addHeader("Content-type", "text/html; charset=utf-8");
		page.setTitle("Error occured");
		if (request.containsKey("state")) {
			if (request.get("state").equals(state)) {
				if (request.containsKey("error")) {
					String errorType = OAuth2ErrorType.valueOf(request.get("error").toUpperCase()).toString();
					page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
					page.addBodyLine("<p>");
					if (errorType != null) {
						errorType = "<strong>" + errorType.replace('_', ' ') + "</strong>";
						if (request.containsKey("error_description")) {
							errorType += ": " + request.get("error_description");
						}
						page.addBodyLine(errorType);
					}
					if (request.containsKey("error_uri")) {
						page.addBodyLine("<br />");
						page.addBodyLine("For more information, visit: <a href=\"" + request.get("error_uri") + "\">" + request.get("error_uri") + "</a>");
					}
					page.addBodyLine("</p>");
				} else {
					if (request.containsKey("code")) {
						tokenParams.put("code", request.get("code"));
						if (!tokenRequest.isAlive()) {
							tokenRequest.start();
						}
						page.setTitle("Authorization successful");
						page.addBodyLine("<p id=\"success\">Authorization successful.</p>");
						page.addBodyLine("<p>You may now close this page and return to the application.</p>");
					} else {
						page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
						page.addBodyLine("<p>Authorization code missing.</p>");
					}
				}
			} else {
				page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
				page.addBodyLine("<p>Mismatch in <code>state</code> parameter.</p>");
			}
		} else {
			page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
			page.addBodyLine("<p>Missing <code>state</code> parameter.</p>");
		}
		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup(OAuth2Settings settings) throws IllegalArgumentException {
		/* validate supplied settings */
		if (settings == null) {
			throw new IllegalArgumentException("Missing settings.");
		}
		if (Utils.isNullOrEmpty(settings.getAuthorizeUri())) {
			throw new IllegalArgumentException("Authorization server URI missing.");
		} else {
			authorizeServer = settings.getAuthorizeUri();
		}
		if (Utils.isNullOrEmpty(settings.getTokenUri())) {
			throw new IllegalArgumentException("Token server URI missing.");
		} else {
			tokenServer = settings.getTokenUri();
		}
		if (Utils.isNullOrEmpty(settings.getClientId())) {
			throw new IllegalArgumentException("Client ID cannot be null or empty.");
		}
		if (Utils.isNullOrEmpty(state)) {
			state = server.generateRandomState(false);
		}
		if (!Utils.isNullOrEmpty(settings.getScope())) {
			authorizeParams.put("scope", settings.getScope());
		}

		/* start listening for incoming redirects */
		try {
			server.start();
			state = server.generateRandomState(true);
		} catch (IllegalStateException e) {
			/* server already running - ignore */
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* set redirect URI if necessary */
		if (Utils.isNullOrEmpty(settings.getRedirectUri())) {
			settings.setRedirectUri(server.getBoundUri());
		}

		/* populate authorization request params */
		authorizeParams.put("client_id", settings.getClientId());
		authorizeParams.put("redirect_uri", settings.getRedirectUri());
		authorizeParams.put("response_type", "code");
		authorizeParams.put("state", state);
		for (Entry<String, String> entry: settings.getExtraAuthorizeParams().entrySet()) {
			if (!authorizeParams.containsKey(entry.getKey())) {
				authorizeParams.put(entry.getKey(), entry.getValue());
			}
		}

		/* populate token request params */
		tokenParams.put("client_id", settings.getClientId());
		if (settings.getClientSecret() != null) {
			tokenParams.put("client_secret", settings.getClientSecret());
		}
		tokenParams.put("grant_type", "authorization_code");
		tokenParams.put("redirect_uri", settings.getRedirectUri());
		for (Entry<String, String> entry: settings.getExtraTokenParams().entrySet()) {
			if (!tokenParams.containsKey(entry.getKey())) {
				tokenParams.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
