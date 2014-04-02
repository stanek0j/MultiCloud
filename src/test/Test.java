package test;

import com.fasterxml.jackson.core.JsonFactory;

import cz.zcu.kiv.multicloud.core.oauth2.AuthorizationCodeGrant;
import cz.zcu.kiv.multicloud.core.oauth2.AuthorizationRequest;
import cz.zcu.kiv.multicloud.core.oauth2.OAuth2Grant;
import cz.zcu.kiv.multicloud.core.oauth2.OAuth2Settings;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JsonFactory jsonFactory = new JsonFactory();

		/*
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
		 */
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
		/*
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
		 */

		OAuth2Settings settings = new OAuth2Settings();
		/* dropbox */
		/*
		settings.setClientId("hq3ir6cgpeynus1");
		settings.setClientSecret("q20xd395442f54b");
		settings.setAuthorizeUri("https://www.dropbox.com/1/oauth2/authorize");
		settings.setTokenUri("https://api.dropbox.com/1/oauth2/token");
		settings.setRedirectUri("https://home.zcu.cz/~stanek0j/multicloud");
		 */

		/* google drive */
		/*
		settings.setClientId("45396671053.apps.googleusercontent.com");
		settings.setClientSecret("ZLa85_y8DSnMoEJ1IsyW3fmm");
		settings.setAuthorizeUri("https://accounts.google.com/o/oauth2/auth");
		settings.setTokenUri("https://accounts.google.com/o/oauth2/token");
		settings.setScope("https://www.googleapis.com/auth/drive");
		 */

		/* onedrive */
		settings.setClientId("00000000400ECF3A");
		settings.setClientSecret("GlPTCgir1pn35eaXlqe29avCnVmSNg5i");
		settings.setAuthorizeUri("https://login.live.com/oauth20_authorize.srf");
		settings.setTokenUri("https://login.live.com/oauth20_token.srf");
		settings.setRedirectUri("https://home.zcu.cz/~stanek0j/multicloud");
		settings.setScope("wl.skydrive_update,wl.offline_access");


		OAuth2Grant grant = new AuthorizationCodeGrant();
		grant.setup(settings);
		AuthorizationRequest request = grant.authorize();
		System.out.println(request);

		System.out.println(grant.getToken().toString());
		System.out.println(grant.getError().toString());

		/*
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
		 */

	}

}
