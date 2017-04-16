package net.orgizm.imgshr;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import net.orgizm.imgshr.Connection;
import net.orgizm.imgshr.InstantAutoCompleteTextView;

public class ShareActivity extends Activity
{
	Context context;
	Intent intent;
	String action;

	InstantAutoCompleteTextView slug;
	Button button;
	TextView status;

	NotificationManager nManager;
	NotificationCompat.Builder nBuilder;
	Random rand = new Random();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		context = getApplicationContext();
		intent = getIntent();
		action = intent.getAction();

		slug   = (InstantAutoCompleteTextView) findViewById(R.id.slug);
		button = (Button) findViewById(R.id.button);
		status = (TextView) findViewById(R.id.status);

		nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(context);

		if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			String[] slugs = getLastSlugs();
			if (slugs != null) {
				String lastSlug = slugs[slugs.length - 1];
				slug.setText(lastSlug, TextView.BufferType.EDITABLE);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, slugs);
				slug.setAdapter(adapter);
			}
		}

		if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
			String path = uri.getPath();

			Matcher m = Pattern.compile("^/!([a-zA-Z0-9]*)").matcher(path);
			String newSlug = null;

			if (m.find()) {
				newSlug = m.group(1);
			}

			if (newSlug != null) {
				setLastSlugs(newSlug);

				CharSequence text = "Saved slug!";

				Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
				toast.show();

				finish();
			}
		}
	}

	private String[] getLastSlugs() {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> set = pref.getStringSet("lastSlugs", null);

		if (set == null) {
			return null;
		} else {
			return set.toArray(new String[set.size()]);
		}
	}

	private void setLastSlugs(String slug) {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		Set<String> set = pref.getStringSet("lastSlugs", null);

		Set<String> setNew;
		if (set == null) {
			setNew = new HashSet<String>();
		} else {
			setNew = new HashSet<String>(set);
		}

		setNew.add(slug);

		editor.putStringSet("lastSlugs", setNew);
		editor.commit();
	}

	public void uploadImageCallback(View view) throws Exception {
		new Thread(new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						slug.setEnabled(false);
						button.setEnabled(false);
						status.setText("Uploading...");
					}
				});

				final String slug = ((InstantAutoCompleteTextView) findViewById(R.id.slug)).getText().toString();
				final int nId = rand.nextInt(2^16);

				nBuilder.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Uploading (" + slug + ")")
					.setProgress(100, 0, false)
					.setContentText("0%")
					.setOngoing(true);

				nManager.notify(nId, nBuilder.build());

				new Thread() {
					@Override
					public void run() {
						try {
							final String message = uploadImages(slug, nId);

							nBuilder.setContentText(message)
								.setProgress(0, 0, false)
								.setOngoing(false);

							nManager.notify(nId, nBuilder.build());

							finish();
						}
						catch (SSLHandshakeException e) {
							Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));

							nBuilder.setContentText("Certificate invalid!")
								.setProgress(0, 0, false)
								.setOngoing(false);

							nManager.notify(nId, nBuilder.build());
						}
						catch (Exception e) {
							Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));
						}
					}
				}.start();

				moveTaskToBack(true);
			}
		}).start();
	}

	private String uploadImages(String slug, int nId) throws Exception {
		ArrayList<Uri> imageUris = null;
		String message = null;

		if (Intent.ACTION_SEND.equals(action)) {
			Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
			if (imageUri != null) {
				imageUris = new ArrayList<Uri>();
				imageUris.add(imageUri);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		}

		setLastSlugs(slug);

		if (imageUris != null) {
			Connection conn = new Connection(context, slug, nManager, nBuilder, nId);

			try {
				message = conn.uploadImages(imageUris);
			}
			finally {
				conn.disconnect();
			}
		}

		return message;
	}
}
