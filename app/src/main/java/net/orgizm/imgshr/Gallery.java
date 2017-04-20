package net.orgizm.imgshr;

public class Gallery {
    private String slug;

    public Gallery() {
    }

    public Gallery(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
