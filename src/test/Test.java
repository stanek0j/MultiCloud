package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.core.oauth2.RedirectServer;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JsonFactory jsonFactory = new JsonFactory();

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpRequestBase httpRequest = new HttpGet("http://echo.jsontest.com/key/value/one/two");
			CloseableHttpResponse response = client.execute(httpRequest);
			System.out.println(response.getStatusLine());
			HeaderIterator it = response.headerIterator();
			while (it.hasNext()) {
				System.out.println(it.next());
			}
			System.out.println();
			BufferedReader br = null;
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					System.out.println(entity.isChunked());
					System.out.println(entity.isRepeatable());
					System.out.println(entity.isStreaming());
					System.out.println(entity.getContentLength());
					System.out.println(entity.getContentEncoding());
					System.out.println(entity.getContentType());
					System.out.println(entity.getContent().available());
					System.out.println();
					/*
					InputStreamReader instream = new InputStreamReader(entity.getContent());
					br = new BufferedReader(instream, 16384);
					br.mark(16384);
					System.out.println(EntityUtils.toString(entity));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
					*/
					JsonParser jp = jsonFactory.createParser(entity.getContent());
					while (jp.nextToken() != null) {
						JsonToken token = jp.getCurrentToken();
						if (token == JsonToken.START_OBJECT) {
							System.out.println("{");
						} else if (token == JsonToken.END_OBJECT) {
							System.out.println("}");
						} else if (token == JsonToken.START_ARRAY) {
							System.out.println("[");
						} else if (token == JsonToken.END_ARRAY) {
							System.out.println("]");
						} else if (token == JsonToken.FIELD_NAME) {
							System.out.print(jp.getCurrentName() + " : ");
						} else {
							System.out.println(jp.getValueAsString());
						}
					}
					jp.close();
				}
			} finally {
				if (br != null) {
					br.close();
				}
			}
			client.close();
			response.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RedirectServer server = RedirectServer.getInstance();
		try {
			server.start();
			System.out.println("Bound to: " + server.getBoundAddress() + ":" + server.getBoundPort());
			Thread.sleep(60000);
		} catch (InterruptedException | IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			server.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
