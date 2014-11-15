package com.ssynhtn.ninegag.data;

/**
 * Created by ssynhtn on 11/14/2014.
 */
public class GagItem {

    String id;
    String caption;
    String imageUrlNormal;
    String imageUrlLarge;

    public GagItem(String id, String caption, String imageUrlNormal, String imageUrlLarge) {
        this.id = id;
        this.caption = caption;
        this.imageUrlNormal = imageUrlNormal;
        this.imageUrlLarge = imageUrlLarge;
    }


    @Override
    public String toString() {
        return id + ", " + caption;
    }
}
