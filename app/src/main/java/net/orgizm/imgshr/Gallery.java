package net.orgizm.imgshr;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class Gallery {
    private String name;
    private String slug;

    public Gallery() {
    }

    public Gallery(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void updateDetailsFromJson(String json) {
        final Gallery gallery = new Gson().fromJson(json, Gallery.class);
        this.name = gallery.getName();
    }
}
