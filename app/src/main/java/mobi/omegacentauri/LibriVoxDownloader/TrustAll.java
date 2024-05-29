package mobi.omegacentauri.LibriVoxDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class TrustAll {
//	static SSLSocketFactory credulousSocketFactory = null;
//
//	static final HostnameVerifier NO_VERIFY = new HostnameVerifier() {
//
//		@Override
//		public boolean verify(String hostname, SSLSession session) {
//			return true;
//		}
//	};
//
//	static final TrustManager credulous = new X509TrustManager() {
//
//		@Override
//		public X509Certificate[] getAcceptedIssuers() {
//			// TODO Auto-generated method stub
//			return new java.security.cert.X509Certificate[] {};
//		}
//
//		@Override
//		public void checkServerTrusted(X509Certificate[] chain, String authType)
//				throws CertificateException {
//		}
//
//		@Override
//		public void checkClientTrusted(X509Certificate[] chain, String authType)
//				throws CertificateException {
//		}
//	};
//
//	static final TrustManager[] credulousOnly = { credulous };

//	public static SSLSocketFactory getCredulousSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
//		if (credulousSocketFactory == null) {
//			SSLContext context = SSLContext.getInstance("TLS");
//			context.init(null, credulousOnly, new java.security.SecureRandom());
//			credulousSocketFactory = context.getSocketFactory();
//		}
//		return credulousSocketFactory;
//	}


	public static InputStream openStream(URL url) throws IOException {
		Log.v("Book", "reading "+url);
		return url.openStream();
//		try {
//			if (true || ! url.getProtocol().equalsIgnoreCase(("https")))
//				return url.openStream();
//			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
//			conn.setSSLSocketFactory(getCredulousSocketFactory());
//		//	conn.setHostnameVerifier(NO_VERIFY);
//			return conn.getInputStream();
//		}
//		catch (KeyManagementException e) {
//			throw new IOException("Trusting "+e);
//		}
//		catch (NoSuchAlgorithmException e) {
//			throw new IOException("Trusting "+e);
//		}
	}
	
}
