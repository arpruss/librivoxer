package mobi.omegacentauri.Librivoxer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.res.AssetManager;
import android.util.Log;

public class Utils {
	public static String getStringFromStream(InputStream in) throws IOException {
		String out = "";
		
		byte[] buffer = new byte[16384];
		int count;
		while(0 <= (count = in.read(buffer, 0, buffer.length))) {
			out += new String(buffer, 0, count);				
		}
		
		return out;
	}
	
	public static String getStringFromURL(URL url) throws IOException {
		return getStringFromStream(url.openStream());
	}

	public static String getStreamFile(InputStream in) {
		try {
			return getStringFromStream(in);
		} catch (IOException e) {
			return "";
		}
	}

	public static String getAssetString(AssetManager assets, String file) {
		try {
			return getStringFromStream(assets.open(file));
		} catch (IOException e) {
			return "";
		}
	}
}
