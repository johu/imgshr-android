package net.orgizm.imgshr;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GalleryListActivity extends Activity {
    private List<Gallery> galleriesList = new ArrayList<>();
    private GalleryListAdapter adapter = new GalleryListAdapter(galleriesList);

    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_manager);

        preferences = new Preferences(getApplicationContext());

        setTitle();
        populateListView();
        registerAddButtonHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateListView();
    }

    protected void setTitle() {
        CollapsingToolbarLayout layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        layout.setTitle(getString(R.string.app_name));
    }

    protected void populateListView() {
        final RecyclerView list = (RecyclerView) findViewById(R.id.list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        list.setLayoutManager(layoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(adapter);

        String[] lastSlugs = preferences.getLastSlugs();

        galleriesList.clear();

        if (lastSlugs != null) {
            for (String slug : lastSlugs) {
                galleriesList.add(new Gallery(slug));
            }
        }

        adapter.notifyDataSetChanged();
    }

    protected void registerAddButtonHandler() {
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleriesList.add(new Gallery("bar"));
                adapter.notifyItemChanged(galleriesList.size() - 1);
                // Toast.makeText(getApplicationContext(), "Whee!", Toast.LENGTH_LONG).show();
            }
        });
    }

}
