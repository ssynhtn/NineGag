package com.ssynhtn.ninegag.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class GagProvider extends ContentProvider {

    private GagDbHelper mGagDbHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int GAGS = 1;
    private static final int GAG_ITEM = 2;

    // initialize uri matcher
    static {
        sUriMatcher.addURI(GagContract.CONTENT_AUTHORITY, GagContract.GagEntry.PATH, GAGS);
        sUriMatcher.addURI(GagContract.CONTENT_AUTHORITY, GagContract.GagEntry.PATH + "/#", GAG_ITEM);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mGagDbHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        int numDeleted = 0;

        switch(code){
            case GAG_ITEM: {
                long id = ContentUris.parseId(uri);
                String mySelection = GagContract.GagEntry.COLUMN_ID + " = " + id;
                if(!TextUtils.isEmpty((selection))){
                    mySelection = selection + " AND " + mySelection;
                }

                numDeleted = db.delete(GagContract.GagEntry.TABLE_NAME, mySelection, selectionArgs);
                break;
            }

            case GAGS: {
                numDeleted = db.delete(GagContract.GagEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new IllegalArgumentException("Unexpected Uri for delete: " + uri);
        }

        if(numDeleted > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return numDeleted;
    }

    @Override
    public String getType(Uri uri) {
        int code = sUriMatcher.match(uri);
        switch(code){
            case GAG_ITEM: return GagContract.GagEntry.CONTENT_TYPE_ITEM;
            case GAGS: return GagContract.GagEntry.CONTENT_TYPE_LIST;
            default: throw new IllegalArgumentException("Unexpected uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mGagDbHelper.getWritableDatabase();
        Uri res = null;

        switch(code){
            case GAG_ITEM: {
                throw new IllegalArgumentException("Can't insert into uri with _id, use update instead: " + uri);
            }

            case GAGS: {
                long id = db.insert(GagContract.GagEntry.TABLE_NAME, null, values);
                if(id != -1){   // if insert success, there are cases of duplicate insertion that will be ignored, which should return -1 as id
                    getContext().getContentResolver().notifyChange(uri, null);
                    res = ContentUris.withAppendedId(GagContract.GagEntry.CONTENT_URI, id);
                }
                break;
            }

            default: throw new IllegalArgumentException("Unexpected Uri for insertion: " + uri);
        }

        return res;
    }

    @Override
    public boolean onCreate() {
        mGagDbHelper = new GagDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mGagDbHelper.getReadableDatabase();

        Cursor cursor;
        switch(code){
            case GAG_ITEM: {
                long id = ContentUris.parseId(uri);
                String mySelection = GagContract.GagEntry.COLUMN_ID + " = " + id;
                if(!TextUtils.isEmpty(selection)){
                    mySelection = selection + " AND " + mySelection;
                }

                cursor = db.query(GagContract.GagEntry.TABLE_NAME, projection, mySelection, selectionArgs, null, null, sortOrder);
                break;
            }

            case GAGS: {
                cursor = db.query(GagContract.GagEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }

            default: throw new IllegalArgumentException("Unexpected uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mGagDbHelper.getWritableDatabase();
        int numInserted = 0;

        // the sunshine app uses try finally here but I really can't see the advantage to do that?
        db.beginTransaction();
        for(ContentValues contentValues : values){
            long id = db.insert(GagContract.GagEntry.TABLE_NAME, null, contentValues);
            if(id != -1)
                numInserted ++;
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        if(numInserted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numInserted;
    }


}
