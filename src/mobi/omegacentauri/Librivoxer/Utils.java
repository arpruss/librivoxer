package mobi.omegacentauri.Librivoxer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class Utils {
	public static String getStringFromURL(URL url) throws IOException {
		String out = "";
		
		InputStream in = url.openStream();
		byte[] buffer = new byte[16384];
		int count;
		while(0 <= (count = in.read(buffer, 0, buffer.length))) {
			out += new String(buffer, 0, count);				
		}
		
		return out;
	}
}
