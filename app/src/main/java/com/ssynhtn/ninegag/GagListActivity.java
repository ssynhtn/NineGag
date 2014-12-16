package com.ssynhtn.ninegag;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class GagListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = GagListActivity.class.getSimpleName();
    private static final String KEY_POSITION = "KEY_POSITION";

    private DrawerLayout mDrawerLayout;
    private ListView mLeftList;
    private ActionBarDrawerToggle mToggle;

    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gag_list);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLeftList = (ListView) findViewById(R.id.drawer_list);

        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, GagConstants.DRAWER_TITLES));
        mLeftList.setOnItemClickListener(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.hello_world, R.string.hello_world);
        mDrawerLayout.setDrawerListener(mToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if (savedInstanceState == null) {
            switchToPage(0);
        } else {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gag_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switchToPage(position);
    }

    private void switchToPage(int position) {
        mPosition = position;

        FragmentManager fm = getSupportFragmentManager();
        GagListFragment fragment = GagListFragment.newInstance(position);
        getSupportActionBar().setTitle(GagConstants.DRAWER_TITLES[position]);
        fm.beginTransaction().replace(R.id.container, fragment).commit();

        mDrawerLayout.closeDrawer(mLeftList);
    }
}
