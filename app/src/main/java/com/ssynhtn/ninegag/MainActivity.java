package com.ssynhtn.ninegag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements AbsListView.OnScrollListener,
        GagItemDownloader.OnDownloadListener,
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = MainActivity.class.getSimpleName();

//    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.gridView)
    StaggeredGridView gridView;
    private MySimpleAdapter adapter;

    @InjectView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView loadingTextView;
    private Button tryLoadButton;
    private ProgressBar mProgressBar;
    private int lastLoadingCount = -1;

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

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View footerView = inflater.inflate(R.layout.grid_view_footer, gridView, false);
        gridView.addFooterView(footerView);

        loadingTextView = (TextView) footerView.findViewById(R.id.loading_text_view);
        mProgressBar = (ProgressBar) footerView.findViewById(R.id.loading_progressBar);
        tryLoadButton = (Button) footerView.findViewById(R.id.button_try_load_again);
        tryLoadButton.setOnClickListener(this);

        adapter = new MySimpleAdapter(this);

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NEXT_PAGE, next);
    }



    public void onClick(View view){
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
            mDownloader.downloadMore();
            return true;
        }
        else if(id == R.id.action_cancel_refresh){
            cancelLoading();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void addItemsToList(List<GagItem> items) {
        adapter.addAll(items);
    }

    private void cancelLoading(){
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        String state;
//        if(scrollState == SCROLL_STATE_FLING)
//            state = "FLING";
//        else if(scrollState == SCROLL_STATE_TOUCH_SCROLL)
//            state = "TOUCH";
//        else
//            state = "IDLE";
//        Log.d(TAG, "onScrollStateChanged: " + state);
    }

    // TODO
    // for the time being, if the internet is not available, this would cause endless trying to load...
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(hasReachedEnd(firstVisibleItem, visibleItemCount, totalItemCount) && totalItemCount != lastLoadingCount){ //&& !loading
            lastLoadingCount = totalItemCount;
            mDownloader.downloadMore();
        }

    }

    private boolean hasReachedEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // when the last item in list becomes visible, this returns true
        return firstVisibleItem + visibleItemCount >= totalItemCount;
    }

    @Override
    public void onDownloadStart(String page) {
        if(page.equals(GagItemDownloader.FIRST_PAGE)){
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.setRefreshing(true);
            // fresh from top
        } else {
//            loadingTextView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            tryLoadButton.setVisibility(View.GONE);

        }

    }

    @Override
    public void onDownloadSuccess(List<GagItem> items, String page, String next) {
        if(page.equals(GagItemDownloader.FIRST_PAGE)){
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        } else {
//            loadingTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
        }
        addItemsToList(items);
    }

    @Override
    public void onDownloadFail(VolleyError error, String page) {
        if(page.equals(GagItemDownloader.FIRST_PAGE)){
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            tryLoadButton.setVisibility(View.VISIBLE);
//            loadingTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
        }


        Toast.makeText(this, "loading failed " + error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GagItem item = (GagItem) parent.getItemAtPosition(position);
        String urlLarge = item.getImageUrlLarge();
        String caption = item.getCaption();

        Intent intent = new Intent(this,GagItemActivity.class);
        intent.putExtra(GagItemActivity.EXTRA_GAG_ITEM, item);
        startActivity(intent);

        Toast.makeText(this, "clicked on item: " + urlLarge + ", caption: " + caption, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        mDownloader.downloadFirst();
    }
}
