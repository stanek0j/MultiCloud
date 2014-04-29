package test;

import java.io.File;

import com.fasterxml.jackson.core.JsonFactory;

import cz.zcu.kiv.multicloud.MultiCloud;
import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.ParentInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2SettingsException;
import cz.zcu.kiv.multicloud.utils.Utils;

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

		final MultiCloud cloud = new MultiCloud();
		cloud.validateAccounts();
		/*
		try {
			AccountInfo info = cloud.getAccountInfo("test");
			if (info == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("User id: " + info.getId());
				System.out.println("User name: " + info.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			AccountQuota quota = cloud.getAccountQuota("test");
			if (quota == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("Quota total: " + quota.getTotalBytes());
				System.out.println("Quota used: " + quota.getUsedBytes());
				System.out.println("Quota free: " + quota.getFreeBytes());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		 */

		String drive = "test";
		//String drive = "googledrive";
		//String drive = "onedrive";

		FileInfo folder = null;
		/*
		try {
			folder = cloud.createFolder(drive, "test", null);
			if (folder == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("folder created: " + folder.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			folder = cloud.rename(drive, folder, "testtest");
			if (folder == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("folder renamed: " + folder.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		 */
		FileInfo target = null;
		try {
			FileInfo list = cloud.listFolder(drive, null);
			if (list == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("list of " + list.getPath() + ":");
				for (FileInfo content: list.getContent()) {
					System.out.println(content.getFileType() + " :: " + content.getId() + " :: " + content.getName() + " :: " + content.getMimeType() + " :: " + Utils.formatSize(content.getSize(), Utils.UnitsFormat.BINARY));
					if (content.getName().equals("test")) {
						folder = content;
					}
					if (content.getName().equals("target")) {
						target = content;
					}
					for (ParentInfo parent: content.getParents()) {
						System.out.println("  parent: " + parent.getId() + " :: " + parent.getPath());
					}
				}
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			FileInfo info = cloud.uploadFile(drive, folder, "test-file.mp3", true, new File("test-file.mp3"));
			if (info == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("file uploaded: " + info.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		/*
		try {
			folder = cloud.move(drive, folder, target, null);
			if (folder == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("folder moved: " + folder.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			FileInfo list = cloud.delete(drive, folder);
			if (list == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("folder deleted: " + list.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		 */

		/*
		try {
			AccountInfo info = cloud.getAccountInfo("googledrive");
			if (info == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("User id: " + info.getId());
				System.out.println("User name: " + info.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			AccountQuota quota = cloud.getAccountQuota("googledrive");
			if (quota == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("Quota total: " + quota.getTotalBytes());
				System.out.println("Quota used: " + quota.getUsedBytes());
				System.out.println("Quota free: " + quota.getFreeBytes());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}

		try {
			AccountInfo info = cloud.getAccountInfo("onedrive");
			if (info == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("User id: " + info.getId());
				System.out.println("User name: " + info.getName());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			AccountQuota quota = cloud.getAccountQuota("onedrive");
			if (quota == null) {
				System.out.println(cloud.getLastError());
			} else {
				System.out.println("Quota total: " + quota.getTotalBytes());
				System.out.println("Quota used: " + quota.getUsedBytes());
				System.out.println("Quota free: " + quota.getFreeBytes());
			}
		} catch (MultiCloudException | OAuth2SettingsException | InterruptedException e1) {
			e1.printStackTrace();
		}
		 */

		/*
		try {
			cloud.createAccount("test", "Dropbox");
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						//cloud.authorizeAccount("test", new TestCallback());
						cloud.authorizeAccount("test", null);
						System.out.println("done");
						//cloud.refreshAccount("test", null);
						//System.out.println("refreshed");
					} catch (MultiCloudException | OAuth2SettingsException
							| InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			Thread.sleep(50000);
			t.interrupt();
			//} catch (MultiCloudException | OAuth2SettingsException e) {
			//} catch (MultiCloudException e) {
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MultiCloudException e1) {
			e1.printStackTrace();
		}
		 */

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/*
		FileCloudManager cm = FileCloudManager.getInstance();
		FileUserManager um = FileUserManager.getInstance();
		try {
			cm.loadCloudSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			um.loadUserSettings();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		 */

		/*
		UserSettings us = new UserSettings();
		us.setUserId("dropbox-01");
		us.setSettingsId("Dropbox");
		us.setTokenId("Xk1tNHPD");
		um.addUserSettings(us);
		 */

		/*
		try {
			um.saveUserSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 */

		//OAuth2Settings settings = Utils.cloudSettingsToOAuth2Settings(cm.getCloudSettings("Dropbox"));

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
		settings.setRefreshToken("1/-_DynXynwO26MR5PLrwE5RMArZz4zRxESYLjAqh5KYc");
		 */

		/* onedrive */
		/*
		settings.setClientId("00000000400ECF3A");
		settings.setClientSecret("GlPTCgir1pn35eaXlqe29avCnVmSNg5i");
		settings.setAuthorizeUri("https://login.live.com/oauth20_authorize.srf");
		settings.setTokenUri("https://login.live.com/oauth20_token.srf");
		settings.setRedirectUri("https://home.zcu.cz/~stanek0j/multicloud");
		settings.setScope("wl.skydrive_update,wl.offline_access");
		settings.setRefreshToken("CtUkV61al976gTo6GRkYZqPEHa493QKRaem4nuAjRfx897lo05N9hGe1hh17vZcPQKql3sER1URZK*8l3jY3F2MG1BP6daandi!hQVj!rxsmuARa2inFTwC49jvurz3OFnCn8IwhGCLby8OXCP7*z8mujt9ZxreH5qrLOoTNDn3psUUbUhhZGYd8uZLalsBMTeKSrY8LqSsDIUi2IdlvSlIJhgfWloKEh07Bbv60CtP3irYhMbD1ZevWRszJ4lUQ9apjz9wt8LEYlLhkpPRR3NkqxjqeF1yPoN5*cWle30pbZWKpV5!Efiya19Rvjh3S80pA!jKBJXFd*UBY4jJhIDjFY!1vsHa0Faayptl2kWOm");
		 */

		/*
		CredentialStore store = new SecureFileCredentialStore("credential-store.sec");
		for (Entry<String, OAuth2Token> entry: store.retrieveAllCredentials()) {
			System.out.println(entry.getKey() + " ==>\n" + entry.getValue());
		}
		 */

		/*
		OAuth2 oauth = new OAuth2(settings, new SecureFileCredentialStore("credential-store.sec"));
		try {
			oauth.authorize(null);
		} catch (OAuth2SettingsException e) {
			e.printStackTrace();
		}
		 */

		/*
		//OAuth2Grant grant = new AuthorizationCodeGrant();
		OAuth2Grant grant = new RefreshTokenGrant();
		try {
			grant.setup(settings);
		} catch (OAuth2SettingsException e1) {
			e1.printStackTrace();
		}
		AuthorizationRequest request = grant.authorize();
		if (request.isActionRequied()) {
			System.out.println(request);
		}

		OAuth2Error error;
		do {
			System.out.println(grant.getToken().toString());
			System.out.println((error = grant.getError()).toString());
			grant.authorize();
			System.out.flush();
		} while (error.getType() != OAuth2ErrorType.SUCCESS);
		System.out.println("Authorization finished.");
		try {
			grant.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 */

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

		System.exit(0);

	}

}
