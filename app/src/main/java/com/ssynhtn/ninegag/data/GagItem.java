package com.ssynhtn.ninegag.data;

import android.content.ContentValues;

import java.io.Serializable;

/**
 * Created by ssynhtn on 11/14/2014.
 */
public class GagItem implements Serializable{

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

    public String getImageUrlNormal() {
        return imageUrlNormal;
    }

    public String getCaption() {
        return caption;
    }

    public String getId() {
        return id;
    }

    public String getImageUrlLarge() {
        return imageUrlLarge;
    }
    @Override
    public String toString() {
        return id + ", " + caption;
    }


    public ContentValues toContentValues(){
        ContentValues values = new ContentValues(4);
        values.put(GagContract.GagEntry.COLUMN_ID, id);
        values.put(GagContract.GagEntry.COLUMN_CAPTION, caption);
        values.put(GagContract.GagEntry.COLUMN_NORMAL_URL, imageUrlNormal);
        values.put(GagContract.GagEntry.COLUMN_LARGE_URL, imageUrlLarge);

        return values;
    }

}
