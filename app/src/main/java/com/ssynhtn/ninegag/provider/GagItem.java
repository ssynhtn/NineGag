package com.ssynhtn.ninegag.provider;

import android.content.ContentValues;
import android.database.Cursor;

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


    // assume the cursor is from GagProvider
    public static GagItem extractFromCursor(Cursor cursor){
        String id = cursor.getString(cursor.getColumnIndex(GagContract.GagEntry.COLUMN_ID));
        String caption = cursor.getString(cursor.getColumnIndex(GagContract.GagEntry.COLUMN_CAPTION));
        String imageUrlNormal = cursor.getString(cursor.getColumnIndex(GagContract.GagEntry.COLUMN_NORMAL_URL));
        String imageUrlLarge = cursor.getString(cursor.getColumnIndex(GagContract.GagEntry.COLUMN_LARGE_URL));

        return new GagItem(id, caption, imageUrlNormal, imageUrlLarge);
    }


    public static ContentValues toContentValues(GagItem gagItem) {
        ContentValues values = new ContentValues(4);
        values.put(GagContract.GagEntry.COLUMN_ID, gagItem.id);
        values.put(GagContract.GagEntry.COLUMN_CAPTION, gagItem.caption);
        values.put(GagContract.GagEntry.COLUMN_NORMAL_URL, gagItem.imageUrlNormal);
        values.put(GagContract.GagEntry.COLUMN_LARGE_URL, gagItem.imageUrlLarge);

        return values;
    }
}
