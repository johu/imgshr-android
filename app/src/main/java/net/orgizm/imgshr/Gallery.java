package net.orgizm.imgshr;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

class Gallery implements Comparable<Gallery> {
    private String name;
    private String slug;

    Gallery(String slug) {
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

    public String toString() {
        if (this.getName() == null) {
            return this.getSlug();
        } else {
            return this.getName();
        }
    }

    void updateDetails(Gallery gallery) {
        this.name = gallery.getName();
    }

    void updateDetails(String json) {
        final Gallery gallery = new Gson().fromJson(json, Gallery.class);
        updateDetails(gallery);
    }

    public int compareTo(@NonNull Gallery other) {
        if (this.getName().equals(other.getName())) {
            return 0;
        } else {
            return -1;
        }
    }
}
