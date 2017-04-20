package net.orgizm.imgshr;

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

                preferences.setLastSlugs(galleriesList);
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        final int position = adapter.getPosition();

        if (item.getItemId() == R.id.delete_from_list) {
            galleriesList.remove(position);
            adapter.notifyDataSetChanged();

            preferences.setLastSlugs(galleriesList);

            Toast.makeText(getApplicationContext(), R.string.gallery_deleted, Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }
}
