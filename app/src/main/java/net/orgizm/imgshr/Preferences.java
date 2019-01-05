package net.orgizm.imgshr;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Preferences {
    final private String PREFERENCES_NAME = "imgshr";
    final private String GALLERIES_KEY = "galleries";
    final private String LOG_TARGET = "net.orgizm.imgshr";

    private SharedPreferences preferences;
    private Gson gson = new Gson();

    Preferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    Set<Gallery> getGalleries() {
        Set<String> serializedGalleries = preferences.getStringSet(GALLERIES_KEY, null);
        Set<Gallery> galleries = new HashSet<>();

        if (serializedGalleries != null) {
            for (String serializedGallery : serializedGalleries) {
                Gallery gallery = gson.fromJson(serializedGallery, Gallery.class);
                galleries.add(gallery);
            }
        }

        return galleries;
    }

    Gallery[] getGalleriesAsArray() {
        Set<Gallery> galleries = getGalleries();

        if (galleries == null) {
            return null;
        } else {
            return galleries.toArray(new Gallery[galleries.size()]);
        }
    }

    void addGallery(Gallery gallery) {
        if (gallery == null) return;

        List<Gallery> newGalleries = new ArrayList<>(getGalleries());

        int position = -1;
        for (int i = 0; i < newGalleries.size(); i++) {
            if (gallery.getSlug().equals(newGalleries.get(i).getSlug())) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            newGalleries.add(gallery);
        } else {
            if (gallery.getShortName() != null) {
                newGalleries.get(position).updateDetails(gallery);
            }
        }

        setGalleries(newGalleries);
    }

    void addGallery(Context context, String slug) {
        final Gallery gallery = new Gallery(slug);

        try {
            final Connection conn = new Connection(context, slug);
            final String json = conn.discoverGallery();

            gallery.updateDetails(json);
        }
        catch(Exception e) {
            Log.d(LOG_TARGET, Log.getStackTraceString(e));
        }

        addGallery(gallery);
    }

    void setGalleries(List<Gallery> galleries) {
        setGalleries(new HashSet<>(galleries));
    }

    void setGalleries(Set<Gallery> galleries) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(GALLERIES_KEY, serializeGalleries(galleries));
        editor.apply();
    }

    Set<String> serializeGalleries(Set<Gallery> galleries) {
        Set<String> serializedGalleries = new HashSet<>();

        for (Gallery gallery : galleries) {
            if (gallery == null) continue;
            serializedGalleries.add(gson.toJson(gallery));
        }

        return serializedGalleries;
    }
}
