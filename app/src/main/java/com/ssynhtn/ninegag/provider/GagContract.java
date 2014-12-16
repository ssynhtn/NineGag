package com.ssynhtn.ninegag.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ssynhtn on 11/18/2014.
 */
public class GagContract {

    public static final String CONTENT_AUTHORITY = "com.ssynhtn.ninegag.provider";
    public static final Uri BASE_CONTNET_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static class GagEntry implements BaseColumns {
        public static final String TABLE_NAME = "gags";
        public static final String PATH = "gags";

        public static final String COLUMN_ID =  "gag_id";   // this id identifies each gag item, and should be unique
        public static final String COLUMN_CAPTION = "caption";
        public static final String COLUMN_NORMAL_URL = "small_url";
        public static final String COLUMN_LARGE_URL = "large_url";


        public static final Uri CONTENT_URI = BASE_CONTNET_URI.buildUpon().appendPath(PATH).build();
        public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH;
    }
}
