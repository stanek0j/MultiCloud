package cz.zcu.kiv.multicloud.core.oauth2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import cz.zcu.kiv.multicloud.core.Utils;

/**
 * cz.zcu.kiv.multicloud.core.oauth2/RedirectServer.java
 *
 * Simple HTTP server listening for OAuth 2.0 authorization code redirect. Its behavior fits the step C of figure 3 of section 4.1. of RFC 6749.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RedirectServer implements Container {

	public static final String DEFAULT_LOCAL_ADDRESS = "127.0.0.1";
	public static final int DEFAULT_LOCAL_PORT = 0;
	public static final int STATE_MIN_LEN = 16;
	public static final int STATE_MAX_LEN = 32;

	protected String boundAddress;
	protected int boundPort;
	protected String localAddress;
	protected int localPort;
	protected RedirectCallback redirectCallback;
	
	protected Connection connection;
	
	public RedirectServer() {
		boundAddress = "";
		boundPort = -1;
		localAddress = DEFAULT_LOCAL_ADDRESS;
		localPort = DEFAULT_LOCAL_PORT;
		redirectCallback = null;
		connection = null;
	}
	
	public String generateRandomState(boolean encodePortNumber) {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		int stateLength = r.nextInt(STATE_MAX_LEN - STATE_MIN_LEN) + STATE_MIN_LEN;
		int length = 0;
		if (encodePortNumber) {
			sb.append(boundPort);
			sb.append('-');
			length = sb.length();
		}
		while (length < stateLength) {
			char ch = (char)r.nextInt();
			if (Utils.isUriLetterOrDigit(ch)) {
				sb.append(ch);
				length++;
			}
		}
		return sb.toString();
	}
	
	public String getBoundAddress() {
		return boundAddress;
	}

	public int getBoundPort() {
		return boundPort;
	}
	
	public String getBoundUri() {
		return "http://" + boundAddress + ":" + boundPort;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String address) {
		localAddress = address;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int port) {
		localPort = port;
	}
	
	public boolean isRunning() {
		return (connection != null);
	}
	
	public RedirectCallback getRedirectCallback() {
		return redirectCallback;
	}

	public void setRedirectCallback(RedirectCallback callback) {
		redirectCallback = callback;
	}

	public void start() throws IllegalStateException, IOException {
		if (connection != null) {
			throw new IllegalStateException("Server already started.");
		}
		Server server = new ContainerServer(this);
		connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(InetAddress.getByName(localAddress), localPort);
		SocketAddress actual = connection.connect(address);
		if (actual instanceof InetSocketAddress) {
			boundAddress = ((InetSocketAddress)actual).getHostString();
			boundPort = ((InetSocketAddress)actual).getPort();
		}
	}
	
	public void stop() throws IOException {
		if (connection != null) {
			connection.close();
			connection = null;
			boundAddress = "";
			boundPort = -1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(Request request, Response response) {
		try {
			Map<String, String> params = new HashMap<>();
			params.putAll(request.getQuery());

			WebPage page = null;
			if (redirectCallback != null) {
				page = redirectCallback.onRedirect(params);
			}
			if (page != null) {
				response.setStatus(page.getStatus());
				for (Entry<String, String> entry: page.getHeaders().entrySet()) {
					response.setValue(entry.getKey(), entry.getValue());
				}
				PrintStream body = response.getPrintStream();
				for (String line: page.getContentLines()) {
					body.println(line);
				}
				body.close();
			} else {
				response.getPrintStream().close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
