package net.orgizm.imgshr;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GalleryAddActivity extends Activity
{
	private TextView slug;
	private Preferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_add_activity);

		slug        = (TextView) findViewById(R.id.slug);
		preferences = new Preferences(getApplicationContext());
	}

	public void addGalleryCallback(View view) {
		preferences.setLastSlugs(new Gallery(slug.getText().toString()));
		Toast.makeText(getApplicationContext(), R.string.gallery_saved, Toast.LENGTH_SHORT).show();
		finish();
	}
}
