package net.orgizm.imgshr;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import net.orgizm.imgshr.InstantAutoCompleteTextView;
import net.orgizm.imgshr.Connection;

public class ShareActivity extends Activity
{
	Context context;
	Intent intent;
	String action;

	InstantAutoCompleteTextView slug;
	Button button;
	TextView status;

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

		if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			String[] slugs = getLastSlugs();
			if(slugs != null) {
				String lastSlug = slugs[slugs.length - 1];
				slug.setText(lastSlug, TextView.BufferType.EDITABLE);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, slugs);
				slug.setAdapter(adapter);
			}
		}
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

		Set<String> setNew;
		if(set == null) {
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

				try {
					final String message = uploadImages();

					runOnUiThread(new Runnable() {
						public void run() {
							status.setText(message);

							if(message.equals("200 OK")) {
								Runnable r = new Runnable() {
									public void run(){
										finish();
									}
								};

								Handler h = new Handler();
								h.postDelayed(r, 2000);
							} else {
								slug.setEnabled(false);
								button.setEnabled(false);
							}
						}
					});
				}
				catch (SSLHandshakeException e) {
					Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));

					runOnUiThread(new Runnable() {
						public void run() {
							status.setText("Certificate invalid!");
						}
					});
				}
				catch (Exception e) {
					Log.d("net.orgizm.imgshr", Log.getStackTraceString(e));
				}
			}
		}).start();
	}

	private String uploadImages() throws Exception {
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

		String slug = ((InstantAutoCompleteTextView) findViewById(R.id.slug)).getText().toString();
		setLastSlugs(slug);

		if (imageUris != null) {
			Connection conn = new Connection(context, slug);

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
