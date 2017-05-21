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
            newGalleries.get(position).updateDetails(gallery);
        }

        setLastSlugs(newGalleries);
    }

    public void setLastSlugs(List<Gallery> galleries) {
        setLastSlugs(new HashSet<>(galleries));
    }

    public void setLastSlugs(Set<Gallery> galleries) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(GALLERIES_KEY, serializeGalleries(galleries));
        editor.apply();
    }

    public Set<String> serializeGalleries(Set<Gallery> galleries) {
        Set<String> serializedGalleries = new HashSet<>();

        for (Gallery gallery : galleries) {
            if (gallery == null) continue;
            serializedGalleries.add(gson.toJson(gallery));
        }

        return serializedGalleries;
    }
}
