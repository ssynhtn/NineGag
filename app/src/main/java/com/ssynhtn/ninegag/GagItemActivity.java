package com.ssynhtn.ninegag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class GagItemActivity extends Activity {

    public static final String EXTRA_GAG_ITEM = "EXTRA_GAG_ITEM";
    private static final String TAG = GagItemActivity.class.getSimpleName();

    @InjectView(R.id.imageView)
    ImageView imageView;

    private GagItem mGagItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gag_item);
        ButterKnife.inject(this);

        handleIntent(getIntent());

        String url = mGagItem.getImageUrlLarge();
        ImageLoader.ImageContainer container = VolleySingleton.getInstance(this).getImageLoader().get(url,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        imageView.setImageBitmap(imageContainer.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        imageView.setImageResource(R.drawable.ic_launcher);
                    }
                });

        String caption = mGagItem.getCaption();
        getActionBar().setTitle(caption);

    }

    private void handleIntent(Intent intent) {
        mGagItem = (GagItem) intent.getSerializableExtra(EXTRA_GAG_ITEM);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gag_item, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        Log.d(TAG, "shareItem: " + shareItem);
//        ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        ShareActionProvider provider = (ShareActionProvider) shareItem.getActionProvider();
        Log.d(TAG, "provider: " + provider);
        provider.setShareIntent(createShareIntent());

        return true;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mGagItem.toString());

        // this is somehow required, else the share button won't be clickable
        intent.setType("text/plain");
        return intent;

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

        return super.onOptionsItemSelected(item);
    }
}
