package cz.zcu.kiv.multicloud.core.oauth2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
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
	
	private static RedirectServer instance;
	
	protected String boundAddress;
	protected int boundPort;
	protected String localAddress;
	protected int localPort;
	protected RedirectCallback redirectCallback;
	
	protected Connection connection;
	
	private RedirectServer() {
		boundAddress = "";
		boundPort = -1;
		localAddress = DEFAULT_LOCAL_ADDRESS;
		localPort = DEFAULT_LOCAL_PORT;
		redirectCallback = null;
		connection = null;
	}
	
	public static RedirectServer getInstance() {
		if (instance == null) {
			instance = new RedirectServer();
		}
		return instance;
	}
	
	public String getBoundAddress() {
		return boundAddress;
	}

	public int getBoundPort() {
		return boundPort;
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
		Container container = getInstance();
		Server server = new ContainerServer(container);
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

	@Override
	public void handle(Request request, Response response) {
		try {
			Map<String, String> params = new HashMap<>();
			Query query = request.getQuery();
			for (Entry<String, String> entry: query.entrySet()) {
				params.put(entry.getKey(), entry.getValue());
				System.out.println(entry.getKey() + " : " + entry.getValue());
			}

			WebPage page;
			if (redirectCallback != null) {
				page = redirectCallback.onRedirect(params);
			}

			PrintStream body = response.getPrintStream();
			body.println("Simple HTTP Server:");
			body.println("  Bound address: " + getBoundAddress());
			body.println("  Bound port: " + getBoundPort());
			body.println();
			body.println("Request params:");
			for (Entry<String, String> entry: params.entrySet()) {
				body.println("  " + entry.getKey() + " = " + entry.getValue());
			}
			body.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
