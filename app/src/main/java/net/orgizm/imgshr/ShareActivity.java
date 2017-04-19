package net.orgizm.imgshr;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Random;
	import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.net.ssl.SSLHandshakeException;

public class ShareActivity extends Activity
{
	final String LOG_TARGET = "net.orgizm.imgshr";

	private Context context;
	private Intent intent;
	private String action;

	private InstantAutoCompleteTextView slug;
	private Button button;
	private TextView status;

	private NotificationManager nManager;
	private NotificationCompat.Builder nBuilder;
	private Random rand = new Random();

	private Preferences preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_activity);

		context = getApplicationContext();
		intent = getIntent();
		action = intent.getAction();

		slug   = (InstantAutoCompleteTextView) findViewById(R.id.slug);
		button = (Button) findViewById(R.id.button);
		status = (TextView) findViewById(R.id.status);

		nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(context);

		preferences = new Preferences(context);

		if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			String[] slugs = preferences.getLastSlugs();
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
				preferences.setLastSlugs(newSlug);

				Toast toast = Toast.makeText(context, getString(R.string.saved_slug), Toast.LENGTH_LONG);
				toast.show();

				finish();
			}
		}
	}

	public void uploadImageCallback(View view) throws Exception {
		new Thread(new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						slug.setEnabled(false);
						button.setEnabled(false);
						status.setText(getString(R.string.uploading, "..."));
					}
				});

				final String slug = ((InstantAutoCompleteTextView) findViewById(R.id.slug)).getText().toString();
				final int nId = rand.nextInt(2^16);

				nBuilder.setSmallIcon(R.mipmap.ic_launcher)
					.setContentTitle(getString(R.string.uploading, " (" + slug + ")"))
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
							Log.d(LOG_TARGET, Log.getStackTraceString(e));

							nBuilder.setContentText(getString(R.string.certificate_invalid))
								.setProgress(0, 0, false)
								.setOngoing(false);

							nManager.notify(nId, nBuilder.build());
						}
						catch (Exception e) {
							Log.d(LOG_TARGET, Log.getStackTraceString(e));
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

		preferences.setLastSlugs(slug);

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
