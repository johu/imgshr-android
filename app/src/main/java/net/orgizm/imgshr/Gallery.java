package net.orgizm.imgshr;

import com.google.gson.Gson;

public class Gallery implements Comparable<Gallery> {
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

    public void updateDetails(Gallery gallery) {
        this.name = gallery.getName();
    }

    public void updateDetails(String json) {
        final Gallery gallery = new Gson().fromJson(json, Gallery.class);
        updateDetails(gallery);
    }

    public int compareTo(Gallery other) {
        if (this.getName() == other.getName()) {
            return 0;
        } else {
            return -1;
        }
    }
}
