package com.ssynhtn.ninegag;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements AbsListView.OnScrollListener, GagItemDownloader.OnDownloadListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

//    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.gridView)
    StaggeredGridView gridView;
    private MySimpleAdapter adapter;

    private TextView loadingTextView;
    private Button tryLoadButton;

    private static final String KEY_NEXT_PAGE = "KEY_NEXT_PAGE";
    private String next;


    private GagItemDownloader mDownloader = new GagItemDownloader(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        if(savedInstanceState != null){
            next = savedInstanceState.getString(KEY_NEXT_PAGE, "0");
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View footerView = inflater.inflate(R.layout.grid_view_footer, gridView, false);
        gridView.addFooterView(footerView);

        loadingTextView = (TextView) footerView.findViewById(R.id.loading_text_view);
        tryLoadButton = (Button) footerView.findViewById(R.id.button_try_load_again);
        tryLoadButton.setOnClickListener(this);

        List<String> list = new ArrayList<String>();
        adapter = new MySimpleAdapter(this, list);

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NEXT_PAGE, next);
    }



    public void onClick(View view){
//        loadContent();
        if(view.getId() == R.id.button_try_load_again)
            mDownloader.downloadMore();
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
//            if(!loading)
//                loadContent();
            mDownloader.downloadMore();
            return true;
        }
        else if(id == R.id.action_cancel_refresh){
            cancelLoading();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//
//    public static final String BASE_URL = "http://infinigag-us.aws.af.cm/hot/";
//    private void loadContent() {
//        final String url = BASE_URL + next;
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject jsonObject) {
////                        Toast.makeText(MainActivity.this, "content loaded: " + jsonObject, Toast.LENGTH_SHORT).show();
//
//                        Log.d(TAG, "json object: " + jsonObject);
//
//                        List<GagItem> items = extractGagItems(jsonObject);
//
//                        String nextPage = extractNext(jsonObject);
//                        if(nextPage != null){
//                            next = nextPage;
//                        }
//
//                        addItemsToList(items);
//                        Toast.makeText(MainActivity.this, "loading finished", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "next is: " + next);
//
//                        loading = false;
//                        loadingTextView.setVisibility(View.GONE);
//
//                    }
//                },
//                new Response.ErrorListener(){
//                    @Override
//                    public void onErrorResponse(VolleyError volleyError) {
//                        // show "Not internet view"
//                        Toast.makeText(MainActivity.this, "loading failed", Toast.LENGTH_SHORT).show();
//                        loading = false;
//                        loadingTextView.setVisibility(View.GONE);
//                    }
//                }
//        );
//
//        request.setTag(this);
//
//        loading = true;
//        loadingTextView.setVisibility(View.VISIBLE);
//        Toast.makeText(this, "start loading new stuff, please wait patiently", Toast.LENGTH_SHORT).show();
//        VolleySingleton.getInstance(this).addToRequestQueue(request);
//
//    }

    private void addItemsToList(List<GagItem> items) {
        List<String> list = new ArrayList<String>();
        for(GagItem item : items){
            list.add(item.toString());
        }

        adapter.addAll(list);
    }
//
//    // normally returns the next page, but if parsing error occurs, returns null
//    private String extractNext(JSONObject jsonObject) {
//        String next = null;
//        try{
//            JSONObject paging = jsonObject.getJSONObject("paging");
//            next = paging.getString("next");
//        }catch(JSONException e){
//            Log.e(TAG, "json parsing error: " + e);
//        }
//
//        return next;
//    }
//
//    private List<GagItem> extractGagItems(JSONObject jsonObject) {
//        List<GagItem> items = new ArrayList<GagItem>();
//
//        try{
//            JSONArray data = jsonObject.getJSONArray("data");
//            int numItems = data.length();
//            for(int i = 0; i < numItems; i++){
//                JSONObject singleItem = data.getJSONObject(i);
//                String id = singleItem.getString("id");
//                String caption = singleItem.getString("caption");
//                JSONObject imageUrls = singleItem.getJSONObject("images");
//                String imageUrlNormal = imageUrls.getString("normal");
//                String imageUrlLarge = imageUrls.getString("large");
//                GagItem item = new GagItem(id, caption, imageUrlNormal, imageUrlLarge);
//                items.add(item);
//            }
//        }catch(JSONException e){
//            Log.e(TAG, "parsing error: " + e);
//        }
//
//        return items;
//    }

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

    // TODO
    // for the time being, if the internet is not available, this would cause endless trying to load...
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        if(hasReachedEnd(firstVisibleItem, visibleItemCount, totalItemCount)){ //&& !loading
////            loadContent();
//            mDownloader.downloadMore();
//        }

    }

    private boolean hasReachedEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // when the last item in list becomes visible, this returns true
        return firstVisibleItem + visibleItemCount >= totalItemCount;
    }

    @Override
    public void onDownloadStart() {
        loadingTextView.setVisibility(View.VISIBLE);
        tryLoadButton.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadSuccess(List<GagItem> items) {
        loadingTextView.setVisibility(View.GONE);
        addItemsToList(items);
    }

    @Override
    public void onDownloadFail(VolleyError error) {
        tryLoadButton.setVisibility(View.VISIBLE);
        loadingTextView.setVisibility(View.GONE);

        Toast.makeText(this, "loading failed", Toast.LENGTH_SHORT).show();
    }




}
