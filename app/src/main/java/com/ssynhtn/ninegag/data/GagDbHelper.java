package com.ssynhtn.ninegag.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ssynhtn on 11/18/2014.
 */
public class GagDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "ninegag.db";
    private static final int DATABASE_VERSION = 1;

    public GagDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createGagTableSql = "CREATE TABLE " + GagContract.GagEntry.TABLE_NAME + " ("
                + GagContract.GagEntry._ID + " INTEGER PRIMARY KEY, "
                + GagContract.GagEntry.COLUMN_ID + " TEXT NOT NULL UNIQUE ON CONFLICT IGNORE, "
                + GagContract.GagEntry.COLUMN_CAPTION + " TEXT, "
                + GagContract.GagEntry.COLUMN_NORMAL_URL + " TEXT NOT NULL, "
                + GagContract.GagEntry.COLUMN_LARGE_URL + " TEXT NOT NULL"
                + ");";

        db.execSQL(createGagTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropGagTableSql = "DROP TABLE " + GagContract.GagEntry.TABLE_NAME + " IF EXISTS;";
        db.execSQL(dropGagTableSql);
        onCreate(db);
    }
}
