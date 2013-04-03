package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://www.seznam.cz");
			HttpResponse response = client.execute(httpget);
			System.out.println(response.getStatusLine());
			HeaderIterator it = response.headerIterator();
			while (it.hasNext()) {
				System.out.println(it.next());
			}
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
			    	InputStreamReader instream = new InputStreamReader(entity.getContent());
			    	br = new BufferedReader(instream, 16384);
			    	br.mark(16384);
			    	//System.out.println(EntityUtils.toString(entity));
			    	String line = null;
			    	while ((line = br.readLine()) != null) {
			    		System.out.println(line);
			    	}
				}
			} finally {
				if (br != null) {
					br.close();
				}
		    }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
