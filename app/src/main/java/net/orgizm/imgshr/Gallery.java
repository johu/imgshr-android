package net.orgizm.imgshr;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

class Gallery implements Comparable<Gallery> {
    private String shortName;
    private String slug;

    Gallery(String slug) {
        this.slug = slug;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String toString() {
        if (this.getShortName() == null) {
            return this.getSlug();
        } else {
            return this.getShortName();
        }
    }

    void updateDetails(Gallery gallery) {
        this.shortName = gallery.getShortName();
    }

    void updateDetails(String json) {
        final Gallery gallery = new Gson().fromJson(json, Gallery.class);
        updateDetails(gallery);
    }

    public int compareTo(@NonNull Gallery other) {
        if (this.getShortName().equals(other.getShortName())) {
            return 0;
        } else {
            return -1;
        }
    }
}
