package net.orgizm.imgshr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryListActivity extends Activity {
    private List<Gallery> galleriesList = new ArrayList<>();
    private GalleryListAdapter adapter = new GalleryListAdapter(galleriesList);

    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_list_activity);

        preferences = new Preferences(getApplicationContext());

        setTitle();
        populateListView();
        registerAddButtonHandler();

        registerForContextMenu(findViewById(R.id.list));
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

        Set<String> lastSlugs = preferences.getLastSlugs();

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
                Intent intent = new Intent(getApplicationContext(), GalleryAddActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        final int position = adapter.getPosition();

        switch (item.getItemId()) {
            case R.id.delete_from_list:
                galleriesList.remove(position);
                adapter.notifyDataSetChanged();

                preferences.setLastSlugs(galleriesList);

                Toast.makeText(getApplicationContext(), R.string.gallery_deleted, Toast.LENGTH_SHORT).show();

                break;

            case R.id.open_url:
                Gallery gallery = galleriesList.get(position);
                String url = "https://imgshr.space/!" + gallery.getSlug();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getApplicationContext().startActivity(intent);

                break;
        }

        return super.onContextItemSelected(item);
    }
}
