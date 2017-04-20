package net.orgizm.imgshr;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Preferences {
    final String PREFERENCES_NAME = "imgshr";
    final String GALLERIES_KEY = "galleries";

    private SharedPreferences preferences;
    private Gson gson = new Gson();

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public Set<Gallery> getGalleries() {
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

    public Set<String> getLastSlugs() {
        Set<Gallery> galleries = getGalleries();
        Set<String> slugs = new HashSet<>();

        for (Gallery gallery : galleries) {
            slugs.add(gallery.getSlug());
        }

        return slugs;
    }

    public String[] getLastSlugsAsArray() {
        Set<String> slugs = getLastSlugs();

        if (slugs == null) {
            return null;
        } else {
            return slugs.toArray(new String[slugs.size()]);
        }
    }

    public void setLastSlugs(Gallery gallery) {
        if (gallery == null) return;

        SharedPreferences.Editor editor = preferences.edit();

        Set<String> serializedGalleries = preferences.getStringSet(GALLERIES_KEY, null);
        serializedGalleries.add(gson.toJson(gallery));

        editor.putStringSet(GALLERIES_KEY, serializedGalleries);
        editor.apply();
    }

    public void setLastSlugs(List<Gallery> galleries) {
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> serializedGalleries = new HashSet<>();

        for (Gallery gallery : galleries) {
            if (gallery == null) continue;
            serializedGalleries.add(gson.toJson(gallery));
        }

        editor.putStringSet(GALLERIES_KEY, serializedGalleries);
        editor.apply();
    }
}
