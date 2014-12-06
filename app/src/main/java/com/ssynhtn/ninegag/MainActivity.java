package com.ssynhtn.ninegag;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.ssynhtn.ninegag.data.GagContract;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


// after a bunch of test, the only conclusion I can get is that: with footer view added, the staggered grid view
// can't give a consistent onScroll parameter, it mostly gives 1 more when entering the app, but gives exactly 0 when exiting
// should try to avoid the footer view wholly
public class MainActivity extends Activity implements AbsListView.OnScrollListener,
        GagItemDownloaderFragment.OnDownloadListener,
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_DOWNLOADER_FRAGMENT = "downloader_fragment";

    @InjectView(R.id.gridView)
    StaggeredGridView gridView;
    private MyCursorAdapter adapter;

    @InjectView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private LinearLayout mLinearLayout;
    private Button tryLoadButton;
    private ProgressBar mProgressBar;

    private GagItemDownloaderFragment mDownloader;

    private boolean mDownloading;
    private boolean mLastLoadingFailed;

    private int mColumnCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mColumnCount = readColumnCountFromPrefs();
        Log.d(TAG, "on create, column count: " + mColumnCount);

        FragmentManager fm = getFragmentManager();
        mDownloader = (GagItemDownloaderFragment) fm.findFragmentByTag(TAG_DOWNLOADER_FRAGMENT);
        if(mDownloader == null) {
            mDownloader = new GagItemDownloaderFragment();
            fm.beginTransaction().add(mDownloader, TAG_DOWNLOADER_FRAGMENT).commit();
        }


        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        LayoutInflater inflater = LayoutInflater.from(this);
        View footerView = inflater.inflate(R.layout.grid_view_footer, gridView, false);
        gridView.addFooterView(footerView);


        mLinearLayout = (LinearLayout) footerView.findViewById(R.id.loading_status);
        mProgressBar = (ProgressBar) footerView.findViewById(R.id.loading_progressBar);
        tryLoadButton = (Button) footerView.findViewById(R.id.button_try_load_again);
        tryLoadButton.setOnClickListener(this);

        adapter = new MyCursorAdapter(this, null, 0);

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setColumnCount(mColumnCount, false);

        getLoaderManager().initLoader(0, null, this);

    }

    private int readColumnCountFromPrefs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(prefs.getString(getString(R.string.key_num_columns),
                getString(R.string.default_num_columns)));
    }

    // NOTE: for some unknown reason, after I tried some variations, the setColumnCount(count, layout) method
    // should be called with layout set to false in onCreate and true in onResume
    @Override
    protected void onResume() {
        super.onResume();
        int count = readColumnCountFromPrefs();
        if(count != mColumnCount){
            mColumnCount = count;
            gridView.setColumnCount(mColumnCount, true);
            Log.d(TAG, "onResume mColumnCount: " + mColumnCount);
        }

    }

    public void onClick(View view){
        if(view.getId() == R.id.button_try_load_again)
            fetchData(false);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // for debug only, if call this when the loading is in progress, the data will be all deleted and ongoing loading will be cancelled
    // but then the onLoadFailed won't be called, the progress bar will hang there forever
    private void clearDatabase() {
        getContentResolver().delete(GagContract.GagEntry.CONTENT_URI, null, null);
        mDownloader.restoreNext();
    }


    private void cancelLoading(){
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    // TODO
    // for the time being, if the internet is not available, this would cause endless trying to load...
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        int headerFooter = gridView.getHeaderViewsCount() + gridView.getFooterViewsCount();
        Log.d(TAG, "firstVisible: " + firstVisibleItem + " visibleItemCount: " + visibleItemCount + " totalItemCount: " + totalItemCount);
        Log.d(TAG, "mDownloading: " + mDownloading + "mLastLoadingFailed: " + mLastLoadingFailed + " headerAndFooter: " + headerFooter);

        if(!mDownloading && !mLastLoadingFailed && visibleItemCount != 0
                && totalItemCount != gridView.getHeaderViewsCount() + gridView.getFooterViewsCount()
                && hasReachedEnd(firstVisibleItem, visibleItemCount, totalItemCount)){ //&& !loading
            fetchData(false);
        }

    }

    private boolean hasReachedEnd(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // when the last item in list becomes visible, this returns true
        // 1 used to load 1 item quicker than really reach end of list
        return firstVisibleItem + visibleItemCount + 1 >= totalItemCount;
    }

    private void fetchData(boolean firstPage){
        mDownloading = true;
        if(firstPage){
            mDownloader.downloadFirst();
        } else {
            mDownloader.downloadMore();
        }
    }

    @Override
    public void onDownloadStart(boolean firstPage) {
        // whenever a download starts, definitely disable the swipe refresh
        mSwipeRefreshLayout.setEnabled(false);

        if(firstPage){
            mSwipeRefreshLayout.setRefreshing(true);
        } else {
            mLinearLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            tryLoadButton.setVisibility(View.GONE);

        }

    }

    @Override
    public void onDownloadSuccess(List<GagItem> items, boolean firstPage) {
        ContentValues[] values = new ContentValues[items.size()];
        for(int i = 0; i < items.size(); i++){
            values[i] = GagItem.toContentValues(items.get(i));
        }

        new BulkInsertTask(values, firstPage).execute();
    }

    @Override
    public void onDownloadFail(VolleyError error, boolean firstPage) {
        mSwipeRefreshLayout.setEnabled(true);

        if(firstPage){
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "loading failed, try refresh again", Toast.LENGTH_SHORT).show();
        } else {
            mLinearLayout.setVisibility(View.VISIBLE);
            tryLoadButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "loading failed, try click on button to load more", Toast.LENGTH_SHORT).show();
        }

        mDownloading = false;
        mLastLoadingFailed = true;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GagItem item = ((MyCursorAdapter.ViewHolder)view.getTag()).mGagItem;

        Intent intent = new Intent(this,GagItemActivity.class);
        intent.putExtra(GagItemActivity.EXTRA_GAG_ITEM, item);
        startActivity(intent);

//        Toast.makeText(this, "clicked on item: " + urlLarge + ", caption: " + caption, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");
        fetchData(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i != 0){
            throw new IllegalArgumentException("unexpected token: " + i);
        }
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String order = GagContract.GagEntry._ID + " ASC";
        return new CursorLoader(this, GagContract.GagEntry.CONTENT_URI, projection, selection, selectionArgs, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor != null && cursor.getCount() > 0){
            adapter.swapCursor(cursor);
        } else {
            Log.d(TAG, "empty cursor, do a total refresh");
            onRefresh();
        }



        // only after the stored data is loaded, do we set the listener, so that the listener won't be triggered
        // when the cursor is null at first
//        gridView.setOnScrollListener(this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }


    public class BulkInsertTask extends AsyncTask<Void, Void, Void>{

        private ContentValues[] values;
        private boolean firstPage;
        public BulkInsertTask(ContentValues[] values, boolean firstPage) {
            super();
            this.values = values;
            this.firstPage = firstPage;
        }


        @Override
        protected Void doInBackground(Void... params) {
            if(firstPage){
                getContentResolver().delete(GagContract.GagEntry.CONTENT_URI, null, null);
            }
            getContentResolver().bulkInsert(GagContract.GagEntry.CONTENT_URI, values);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mSwipeRefreshLayout.setEnabled(true);

            if(firstPage){
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }

            mDownloading = false;
            mLastLoadingFailed = false;
        }
    }
}
