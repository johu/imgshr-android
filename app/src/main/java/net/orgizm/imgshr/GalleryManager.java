package net.orgizm.imgshr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.IntegerRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryManager extends Activity {
    private List<Gallery> galleriesList = new ArrayList<>();
    private GalleryListAdapter adapter = new GalleryListAdapter(galleriesList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_manager);

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

        SharedPreferences pref = getSharedPreferences("imgshr", Context.MODE_PRIVATE);
        Set<String> set = pref.getStringSet("lastSlugs", null);

        galleriesList.clear();

        if (set != null) {
            for (String slug : set) {
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
