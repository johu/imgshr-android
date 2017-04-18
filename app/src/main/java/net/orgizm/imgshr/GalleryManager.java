package net.orgizm.imgshr;

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

        galleriesList.add(new Gallery("foo"));
        galleriesList.add(new Gallery("bar"));

        Toast.makeText(getApplicationContext(), Integer.toString(galleriesList.size()), Toast.LENGTH_LONG).show();

        adapter.notifyDataSetChanged();
    }

    protected void registerAddButtonHandler() {
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleriesList.add(new Gallery("bar"));
                adapter.notifyItemChanged(galleriesList.size() - 1);
                Toast.makeText(getApplicationContext(), "Whee!", Toast.LENGTH_LONG).show();
            }
        });
    }

}
