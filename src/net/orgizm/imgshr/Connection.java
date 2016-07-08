package net.orgizm.imgshr;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Connection
{
	Context context;
	HttpURLConnection conn;
	OutputStream out;

	final String PARAM    = "picture[image][]";
	final String BOUNDARY = "*****";
	final String CRLF     = "\r\n";

	public Connection(Context context, String slug) throws Exception {
		this(context, slug, null);
	}

	public Connection(Context context, String slug, String endpoint) throws Exception {
		URL url;

		Boolean https   = false;
		Boolean pinning = false;

		if (endpoint == null) {
			endpoint = "https://imgshr.space/api";
			pinning = true;
		}

		if (endpoint.startsWith("https://")) {
			https = true;
		}

		this.context = context;

		url = new URL(endpoint + "/!" + slug);

		if (https) {
			initializeEncryption(pinning);
			conn = (HttpsURLConnection) url.openConnection();
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}

		setConnectionProperties();

		out = new BufferedOutputStream(conn.getOutputStream());
	}

	public void disconnect() {
		conn.disconnect();
	}

	public void uploadImage(Uri imageUri) throws FileNotFoundException, IOException {
		ContentResolver cr = context.getContentResolver();
		InputStream file = cr.openInputStream(imageUri);

		String filename = getFileName(cr, imageUri);
		String cd = "Content-Disposition: form-data; name=\"" + PARAM + "\"; filename=\"" + filename + "\"" + CRLF;
		String ct = "Content-Type: " + cr.getType(imageUri) + CRLF;

		out.write(("--" + BOUNDARY + CRLF).getBytes());
		out.write(cd.getBytes());
		out.write(ct.getBytes());
		out.write(CRLF.getBytes());

		byte[] buffer = new byte[256];
		int bytesRead = 0;
		while ((bytesRead = file.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}

		out.write(CRLF.getBytes());
	}

	public String uploadImages(ArrayList<Uri> imageUris) throws FileNotFoundException, IOException {
		for (Uri imageUri: imageUris) {
			uploadImage(imageUri);
		}

		out.write(("--" + BOUNDARY + "--" + CRLF).getBytes());
		out.flush();

		int code = conn.getResponseCode();
		String message = conn.getResponseMessage();

		Log.i("net.orgizm.imgshr", "HTTP Response: " + code + " " + message);

		return code + " " + message;
	}

	private String getFileName(ContentResolver cr, Uri uri) {
		String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
		Cursor metaCursor = cr.query(uri, projection, null, null, null);
		String fileName = null;

		if (metaCursor != null) {
			try {
				if (metaCursor.moveToFirst()) {
					fileName = metaCursor.getString(0);
				}
 			}
			finally {
				metaCursor.close();
			}
		}

		return fileName;
	}

	private void initializeEncryption(Boolean pinning) throws Exception {
		if (pinning) {
			initializePinning();
		} else {
			initializeTrustAllCerts();
		}
	}

	private void initializePinning() throws CertificateException, IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		AssetManager assetManager = this.context.getAssets();
		InputStream keyStoreInputStream = assetManager.open("net.orgizm.imgshr.bks");
		KeyStore trustStore = KeyStore.getInstance("BKS");

		trustStore.load(keyStoreInputStream, "ahw0Iewiefei6jee".toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(trustStore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	}

	private void initializeTrustAllCerts() throws KeyManagementException, NoSuchAlgorithmException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
		};
 
		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
 
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	private void setConnectionProperties() throws ProtocolException {
		conn.setDoOutput(true);
		conn.setChunkedStreamingMode(0);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
	}
}
