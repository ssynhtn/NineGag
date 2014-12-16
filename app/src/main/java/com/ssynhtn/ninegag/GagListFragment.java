package com.ssynhtn.ninegag;

/**
 * Created by PC on 2014/12/16.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.ssynhtn.ninegag.provider.GagContract;
import com.ssynhtn.ninegag.provider.GagItem;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class GagListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener,
        AbsListView.OnScrollListener ,AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>,
        GagItemDownloaderFragment.OnDownloadListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_DOWNLOADER_FRAGMENT = "downloader_fragment";

    StaggeredGridView gridView;
    private MyCursorAdapter adapter;

    SwipeRefreshLayout mSwipeRefreshLayout;

    private LinearLayout mLinearLayout;
    private Button tryLoadButton;
    private ProgressBar mProgressBar;

    private GagItemDownloaderFragment mDownloader;

    private boolean mDownloading;
    private boolean mLastLoadingFailed;

    private int mColumnCount;



    public GagListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gag_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View rootView = getView();
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        gridView = (StaggeredGridView) rootView.findViewById(R.id.gridView);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        mColumnCount = readColumnCountFromPrefs();

        FragmentManager fm = getChildFragmentManager();
        mDownloader = (GagItemDownloaderFragment) fm.findFragmentByTag(TAG_DOWNLOADER_FRAGMENT);
        if(mDownloader == null) {
            mDownloader = new GagItemDownloaderFragment();
            fm.beginTransaction().add(mDownloader, TAG_DOWNLOADER_FRAGMENT).commit();
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.holo_blue,
                R.color.holo_green,
                R.color.holo_red,
                R.color.holo_orange);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View footerView = inflater.inflate(R.layout.grid_view_footer, gridView, false);
        gridView.addFooterView(footerView);


        mLinearLayout = (LinearLayout) footerView.findViewById(R.id.loading_status);
        mProgressBar = (ProgressBar) footerView.findViewById(R.id.loading_progressBar);
        tryLoadButton = (Button) footerView.findViewById(R.id.button_try_load_again);
        tryLoadButton.setOnClickListener(this);

        adapter = new MyCursorAdapter(getActivity(), null, 0);

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setColumnCount(mColumnCount, false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        int count = readColumnCountFromPrefs();
        if(count != mColumnCount){
            mColumnCount = count;
            gridView.setColumnCount(mColumnCount, true);
            Log.d(TAG, "onResume mColumnCount: " + mColumnCount);
        }

    }

    private int readColumnCountFromPrefs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return Integer.parseInt(prefs.getString(getString(R.string.key_num_columns),
                getString(R.string.default_num_columns)));
    }

    @Override
    public void onRefresh() {
        fetchData(true);
    }

    private void fetchData(boolean firstPage){
        mDownloading = true;
        if(firstPage){
            mDownloader.downloadFirst();
        } else {
            mDownloader.downloadMore();
        }
    }

    public void onClick(View view){
        if(view.getId() == R.id.button_try_load_again)
            fetchData(false);
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
        return new CursorLoader(getActivity(), GagContract.GagEntry.CONTENT_URI, projection, selection, selectionArgs, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor != null && cursor.getCount() > 0){
            adapter.swapCursor(cursor);
        } else {
            Log.d(TAG, "empty cursor, do a total refresh");
            onRefresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GagItem item = ((MyCursorAdapter.ViewHolder)view.getTag()).mGagItem;

        Intent intent = new Intent(getActivity(), GagItemActivity.class);
        intent.putExtra(GagItemActivity.EXTRA_GAG_ITEM, item);
        startActivity(intent);

//        Toast.makeText(this, "clicked on item: " + urlLarge + ", caption: " + caption, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "loading failed, try refresh again", Toast.LENGTH_SHORT).show();
        } else {
            mLinearLayout.setVisibility(View.VISIBLE);
            tryLoadButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "loading failed, try click on button to load more", Toast.LENGTH_SHORT).show();
        }

        mDownloading = false;
        mLastLoadingFailed = true;

    }

    public class BulkInsertTask extends AsyncTask<Void, Void, Void> {

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
                getActivity().getContentResolver().delete(GagContract.GagEntry.CONTENT_URI, null, null);
            }
            getActivity().getContentResolver().bulkInsert(GagContract.GagEntry.CONTENT_URI, values);
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