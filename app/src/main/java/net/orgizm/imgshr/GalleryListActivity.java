package net.orgizm.imgshr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryListActivity extends Activity {
    final String LOG_TARGET = "net.orgizm.imgshr";

    private List<Gallery> galleriesList = new ArrayList<>();
    private GalleryListAdapter adapter = new GalleryListAdapter(galleriesList);

    private Preferences preferences;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_list_activity);

        preferences = new Preferences(getApplicationContext());
        context = getApplicationContext();

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

        galleriesList.clear();
        galleriesList.addAll(preferences.getGalleries());

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
                String url1 = "https://imgshr.space/!" + galleriesList.get(position).getSlug();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);

                break;

            case R.id.update_details:
                final Gallery gallery1 = galleriesList.get(position);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            final Connection conn = new Connection(context, gallery1.getSlug());
                            final String json = conn.discoverGallery();

                            Log.d(LOG_TARGET, json);

                            gallery1.updateDetails(json);
                        }
                        catch(Exception e) {
                            Log.d(LOG_TARGET, Log.getStackTraceString(e));
                        }

                        preferences.setLastSlugs(gallery1);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, R.string.gallery_saved, Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

                break;

            case R.id.share_url:
                Intent intent2 = new Intent();
                String url2 = "https://imgshr.space/!" + galleriesList.get(position).getSlug();

                intent2.setAction(Intent.ACTION_SEND);
                intent2.putExtra(Intent.EXTRA_TEXT, url2);
                intent2.setType("text/plain");

                startActivity(intent2);

                break;
        }

        return super.onContextItemSelected(item);
    }
}
