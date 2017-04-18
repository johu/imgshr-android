package net.orgizm.imgshr;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;

public class GalleryManager extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_manager);

        CollapsingToolbarLayout layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        layout.setTitle(getString(R.string.app_name));
    }

}
