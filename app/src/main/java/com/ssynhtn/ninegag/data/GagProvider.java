package com.ssynhtn.ninegag.data;

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

    private static UriMatcher sUriMatcher;

    private static final int GAGS = 1;
    private static final int GAG_ITEM = 2;

    // initialize uri matcher
    static {
        sUriMatcher.addURI(GagContract.CONTENT_AUTHORITY, GagContract.GagEntry.PATH, GAGS);
        sUriMatcher.addURI(GagContract.CONTENT_AUTHORITY, GagContract.GagEntry.PATH, GAG_ITEM);
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
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }

            case GAGS: {
                numDeleted = db.delete(GagContract.GagEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }

            default:
                throw new IllegalArgumentException("Unexpected Uri for delete: " + uri);
        }

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
                throw new IllegalArgumentException("Can't insert into uri with id, use update instead: " + uri);
            }

            case GAGS: {
                long id = db.insert(GagContract.GagEntry.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                res = ContentUris.withAppendedId(GagContract.GagEntry.CONTENT_URI, id);
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
}
