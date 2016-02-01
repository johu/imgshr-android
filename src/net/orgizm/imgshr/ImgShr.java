package net.orgizm.imgshr;

import android.app.Activity;
import android.os.Bundle;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import net.orgizm.imgshr.InstantAutoCompleteTextView;

public class ImgShr extends Activity
{
	Boolean DEBUG = false;
	Boolean PINNING = true;
	Intent intent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		intent = getIntent();

		InstantAutoCompleteTextView slug = (InstantAutoCompleteTextView) findViewById(R.id.slug);
		Button button = (Button) findViewById(R.id.button);
		TextView text = (TextView) findViewById(R.id.status);

		String[] slugs = getLastSlugs();
		if(slugs != null) {
			String lastSlug = slugs[slugs.length - 1];
			slug.setText(lastSlug, TextView.BufferType.EDITABLE);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, slugs);
			slug.setAdapter(adapter);
		}

		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri == null) {
			slug.setEnabled(false);
			button.setEnabled(false);
			text.setText("Start app via share function!");

			Runnable r = new Runnable() {
				public void run(){
					finish();
				}
			};

			Handler h = new Handler();
			h.postDelayed(r, 3000);
		}
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

	private String[] getLastSlugs() {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> set = pref.getStringSet("lastSlugs", null);

		if(set == null) {
			return null;
		} else {
			return set.toArray(new String[set.size()]);
		}
	}

	private void setLastSlugs(String slug) {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		Set<String> set = pref.getStringSet("lastSlugs", null);

		if(set != null) {
			Set<String> setNew = new HashSet<String>(set);
			setNew.add(slug);

			editor.putStringSet("lastSlugs", setNew);
			editor.commit();
		}
	}

	public void uploadImageCallback(View view) throws Exception {
		final TextView text = (TextView) findViewById(R.id.status);

		new Thread(new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						text.setText("Uploading...");
					}
				});

				try {
					final String message = uploadImage();

					runOnUiThread(new Runnable() {
						public void run() {
							text.setText(message);

							if(message.equals("200 OK")) {
								finish();
							}
						}
					});
				}
				catch (SSLHandshakeException e) {
					Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));

					runOnUiThread(new Runnable() {
						public void run() {
							text.setText("Certificate invalid!");
						}
					});
				}
				catch (Exception e) {
					Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));
				}
			}
		}).start();
	}

	private String uploadImage() throws Exception {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

		String slug = ((InstantAutoCompleteTextView) findViewById(R.id.slug)).getText().toString();
		setLastSlugs(slug);

		String url = "https://imgshr.orgizm.net/api/!" + slug;
		if(DEBUG) {
			url = "http://10.0.2.2:3000/api/!" + slug;
		}

		String message = null;

		if (imageUri != null) {
			ContentResolver cr = getContentResolver();
			InputStream file = cr.openInputStream(imageUri);

			if(!DEBUG) {
				if(PINNING) {
					AssetManager assetManager = getAssets();
					InputStream keyStoreInputStream = assetManager.open("net.orgizm.imgshr.bks");
					KeyStore trustStore = KeyStore.getInstance("BKS");

					trustStore.load(keyStoreInputStream, "ahw0Iewiefei6jee".toCharArray());

					TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
					tmf.init(trustStore);

					SSLContext sslContext = SSLContext.getInstance("TLS");
					sslContext.init(null, tmf.getTrustManagers(), null);
					HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
				} else {
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
			}

			String param    = "picture[image][]";
			String filename = getFileName(cr, imageUri);
			String boundary = "*****";
			String crlf     = "\r\n";
			String cd       = "Content-Disposition: form-data; name=\"" + param + "\"; filename=\"" + filename + "\"" + crlf;
			String ct       = "Content-Type: " + cr.getType(imageUri) + crlf;

			HttpURLConnection conn = null;
			if(DEBUG) {
				conn = (HttpURLConnection) (new URL(url)).openConnection();
			} else {
				conn = (HttpsURLConnection) (new URL(url)).openConnection();
			}

			try {
				conn.setDoOutput(true);
				conn.setChunkedStreamingMode(0);

				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

				OutputStream out = new BufferedOutputStream(conn.getOutputStream());

				out.write(("--" + boundary + crlf).getBytes());
				out.write(cd.getBytes());
				out.write(ct.getBytes());
				out.write(crlf.getBytes());

				byte[] buffer = new byte[256];
				int bytesRead = 0;
				while ((bytesRead = file.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}

				out.write(crlf.getBytes());
				out.write(("--" + boundary + "--" + crlf).getBytes());

				out.flush();

				int responseCode = 0;
				responseCode = conn.getResponseCode();
				String responseMessage = conn.getResponseMessage();

				Log.i("net.orgizm.imgshr", "HTTP Response: " + responseCode + " " + responseMessage);

				message = responseCode + " " + responseMessage;
			}
			finally {
				conn.disconnect();
			}
		}

		return message;
	}
}
