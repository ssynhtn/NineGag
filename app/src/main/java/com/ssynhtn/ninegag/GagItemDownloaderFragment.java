package com.ssynhtn.ninegag;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssynhtn on 11/15/2014.
 */
public class GagItemDownloaderFragment extends Fragment {
    private static final String BASE_URL = "http://infinigag-us.aws.af.cm/hot/";
    public static final String FIRST_PAGE = "0";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_NEXT = "KEY_NEXT";

    private String next;
    private OnDownloadListener mListener;

    private boolean loading;
//    private boolean lastLoadFailed;

//    public GagItemDownloaderFragment(Context context, OnDownloadListener listener) {
//        this.context = context;
//        mListener = listener;
//    }


    public GagItemDownloaderFragment() {
        super();
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        next = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(KEY_NEXT, FIRST_PAGE);
        Log.d(TAG, "Fragment onCreate, get next: " + next);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(KEY_NEXT, next).commit();
        Log.d(TAG, "Fragment onDestroy, saving next: " + next);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            this.mListener = (OnDownloadListener) activity;
        } catch (ClassCastException e){
            Log.e(TAG, "class " + activity.getClass() + " must implement " + OnDownloadListener.class + " interface");
        }
    }

    public static interface OnDownloadListener {
        void onDownloadStart(String page);   // give listener a chance to show progress and stuff
        void onDownloadSuccess(List<GagItem> items, String page, String next);
        void onDownloadFail(VolleyError error, String page);
//        void onNoHandle();  // download request was not handled for some reason, e.g. last downloading is in progress
    }


    public void downloadFirst(){
        downloadMore(FIRST_PAGE);
    }

    public void downloadMore(){
        if(next == null){
            next = FIRST_PAGE;
        }
        downloadMore(next);
    }

    private void downloadMore(final String page){
        if(loading) {
            Log.d(TAG, "try loading page: " + page + " but is already loading, cancelled");
            return;
        } else {
            Log.d(TAG, "try loading page: " + page);
        }

        String url = BASE_URL + page;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
//                        Toast.makeText(MainActivity.this, "content loaded: " + jsonObject, Toast.LENGTH_SHORT).show();

//                        Log.d(TAG, "json object: " + jsonObject);

                        List<GagItem> items = extractGagItems(jsonObject);

                        String nextPage = extractNext(jsonObject);
                        if(nextPage != null){
                            next = nextPage;
                            Log.d(TAG,  "next page is: " + next);
                        } else {
                            throw new RuntimeException("next page is null!");
                        }

                        mListener.onDownloadSuccess(items, page, nextPage);
                        loading = false;

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading = false;
                        mListener.onDownloadFail(volleyError, page);
                    }
                }
        );

        request.setTag(this);

        loading = true;
        mListener.onDownloadStart(page);
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);

    }

    // normally returns the next page, but if parsing error occurs, returns null
    private String extractNext(JSONObject jsonObject) {
        String next = null;
        try{
            JSONObject paging = jsonObject.getJSONObject("paging");
            next = paging.getString("next");
        }catch(JSONException e){
            Log.e(TAG, "json parsing error: " + e);
        }

        return next;
    }

    private List<GagItem> extractGagItems(JSONObject jsonObject) {
        List<GagItem> items = new ArrayList<GagItem>();

        try{
            JSONArray data = jsonObject.getJSONArray("data");
            int numItems = data.length();
            for(int i = 0; i < numItems; i++){
                JSONObject singleItem = data.getJSONObject(i);
                String id = singleItem.getString("id");
                String caption = singleItem.getString("caption");
                JSONObject imageUrls = singleItem.getJSONObject("images");
                String imageUrlNormal = imageUrls.getString("normal");
                String imageUrlLarge = imageUrls.getString("large");
                GagItem item = new GagItem(id, caption, imageUrlNormal, imageUrlLarge);
                items.add(item);
            }
        }catch(JSONException e){
            Log.e(TAG, "parsing error: " + e);
        }

        return items;
    }


    public void restoreNext(){
        VolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll(this);
        loading = false;
        next = FIRST_PAGE;
    }

}
