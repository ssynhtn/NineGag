package com.ssynhtn.ninegag;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements AbsListView.OnScrollListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.listView) ListView listView;
    private ArrayAdapter<String> adapter;

    private static final String KEY_NEXT_PAGE = "KEY_NEXT_PAGE";
    private String next;

    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        if(savedInstanceState != null){
            next = savedInstanceState.getString(KEY_NEXT_PAGE, "0");
        }

        String[] data = "hello world google chrome android world".split(" ");
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(data));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnScrollListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NEXT_PAGE, next);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_refresh){
            if(!loading)
                loadContent();
            return true;
        }
        else if(id == R.id.action_cancel_refresh){
            cancelLoading();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static final String BASE_URL = "http://infinigag-us.aws.af.cm/hot/";
    private void loadContent() {
        final String url = BASE_URL + next;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
//                        Toast.makeText(MainActivity.this, "content loaded: " + jsonObject, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "json object: " + jsonObject);

                        List<GagItem> items = extractGagItems(jsonObject);

                        String nextPage = extractNext(jsonObject);
                        if(nextPage != null){
                            next = nextPage;
                        }

                        addItemsToList(items);
                        Toast.makeText(MainActivity.this, "loading finished", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "next is: " + next);

                        loading = false;

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // show "Not internet view"
                        Toast.makeText(MainActivity.this, "loading failed", Toast.LENGTH_SHORT).show();
                        loading = false;
                    }
                }
        );

        request.setTag(this);

        loading = true;
        Toast.makeText(this, "start loading new stuff, please wait patiently", Toast.LENGTH_SHORT).show();
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private void addItemsToList(List<GagItem> items) {
        for(GagItem item : items){
            adapter.add(item.toString());
        }
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

    private void cancelLoading(){
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        String state;
        if(scrollState == SCROLL_STATE_FLING)
            state = "FLING";
        else if(scrollState == SCROLL_STATE_TOUCH_SCROLL)
            state = "TOUCH";
        else
            state = "IDLE";
        Log.d(TAG, "onScrollStateChanged: " + state);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(hasReachedEnd(firstVisibleItem, visibleItemCount, totalItemCount) && !loading){
            loadContent();
        }
    }

    private boolean hasReachedEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // when the last item in list becomes visible, this returns true
        return firstVisibleItem + visibleItemCount >= totalItemCount;
    }


    private class LoadJsonTask extends AsyncTask<String, Void, JSONObject> {
        public static final String BASE_URL = "http://www.baidu.com/";

        @Override
        protected JSONObject doInBackground(String... params) {
            String page = params[0];
            String url = BASE_URL + page;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {

                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    }
            );



            return null;
        }
    }

}
