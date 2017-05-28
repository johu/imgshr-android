package net.orgizm.imgshr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GalleryAddActivity extends Activity
{
    private Context context;

    private TextView slug;
	private Preferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_add_activity);

        context     = getApplicationContext();
		slug        = (TextView) findViewById(R.id.slug);
		preferences = new Preferences(context);
	}

	public void addGalleryCallback(View view) {
        new Thread(new Runnable() {
            public void run() {
                final String s = slug.getText().toString();
                preferences.addGallery(context, s);

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, R.string.gallery_saved, Toast.LENGTH_SHORT).show();
                    }
                });

                finish();
            }
        }).start();
	}
}
