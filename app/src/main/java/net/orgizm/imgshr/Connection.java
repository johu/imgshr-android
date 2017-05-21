package net.orgizm.imgshr;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
    private final String DEFAULT_API_URL = "https://imgshr.space/api";

	private Context context;
	private HttpURLConnection conn;
	private OutputStream out;
	private NotificationManager nManager;
	private NotificationCompat.Builder nBuilder;
    private String slug;

	private final String PARAM    = "picture[image][]";
	private final String BOUNDARY = "*****";
	private final String CRLF     = "\r\n";

	private int nId = 0;

	public Connection(Context context, String slug) throws Exception {
		this(context, slug, null, null, null, 0);
	}

	public Connection(Context context, String slug, NotificationManager nManager, NotificationCompat.Builder nBuilder) throws Exception {
		this(context, slug, null, nManager, nBuilder, 0);
	}

	public Connection(Context context, String slug, NotificationManager nManager, NotificationCompat.Builder nBuilder, int nId) throws Exception {
		this(context, slug, null, nManager, nBuilder, nId);
	}

	public Connection(Context context, String slug, String endpoint, NotificationManager nManager, NotificationCompat.Builder nBuilder, int nId) throws Exception {
		URL url;

		Boolean https   = false;
		Boolean pinning = false;

		if (endpoint == null) {
			endpoint = DEFAULT_API_URL;
			pinning = true;
		}

		if (endpoint.startsWith("https://")) {
			https = true;
		}

		this.context = context;
		this.nManager = nManager;
		this.nBuilder = nBuilder;
		this.nId = nId;
        this.slug = slug;

		url = new URL(endpoint + "/!" + slug);

		if (https) {
			initializeEncryption(pinning);
			conn = (HttpsURLConnection) url.openConnection();
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
    }

	public void disconnect() {
		conn.disconnect();
	}

	private void uploadImage(Uri imageUri, int i, int n) throws IOException, InstantiationException, IllegalAccessException {
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
		int size = file.available();
		int written = 0;
		int percent = 0;
		int lastPercent = 0;

		String overall = "";
		if (n > 1) {
			i++;
			overall = " (" + i + " of " + n + ")";
		}

		while ((bytesRead = file.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);

			written += bytesRead;
			percent = written * 100 / size;

			if (percent != lastPercent) {
				nBuilder.setProgress(size, written, false)
					.setContentText("" + percent + "%" + overall);
				nManager.notify(nId, nBuilder.build());
				lastPercent = percent;
			}
		}

		out.write(CRLF.getBytes());
	}

	public String uploadImages(ArrayList<Uri> imageUris) throws IOException, InstantiationException, IllegalAccessException  {
        out = new BufferedOutputStream(conn.getOutputStream());
        setConnectionPropertiesForUpload();

        int n = imageUris.size();
		for(int i = 0; i < n; i++) {
			uploadImage(imageUris.get(i), i, n);
		}

		out.write(("--" + BOUNDARY + "--" + CRLF).getBytes());
		out.flush();

		int code = conn.getResponseCode();
		String message = conn.getResponseMessage();

		Log.i("net.orgizm.imgshr", "HTTP upload Response: " + code + " " + message);

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

	@SuppressLint({"TrulyRandom", "BadHostnameVerifier", "TrustAllX509TrustManager"})
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

	public void setConnectionPropertiesForUpload() throws ProtocolException {
		conn.setDoOutput(true);
		conn.setChunkedStreamingMode(0);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
	}

    public void setConnectionPropertiesForDiscover() throws ProtocolException {
        conn.setRequestMethod("GET");
    }

    public String discoverGallery() throws IOException {
        setConnectionPropertiesForDiscover();

        int code = conn.getResponseCode();
        String message = conn.getResponseMessage();

        Log.i("net.orgizm.imgshr", "HTTP discover Response: " + code + " " + message);

        String json = "";

        try {
            InputStream in = conn.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            int data = reader.read();
            while (data != -1) {
                json += (char) data;
                data = reader.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return json;
    }
}
