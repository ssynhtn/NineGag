package com.ssynhtn.ninegag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.service.SaveImageFileService;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoViewAttacher;


public class GagItemActivity extends Activity implements PhotoViewAttacher.OnViewTapListener {

    public static final String EXTRA_GAG_ITEM = "EXTRA_GAG_ITEM";
    private static final String TAG = GagItemActivity.class.getSimpleName();

    // to avoid image too large problems
    private static final int MAX_WIDTH = 2048;
    private static final int MAX_HEIGHT = MAX_WIDTH;

    @InjectView(R.id.imageView)
    ImageView imageView;

    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    @InjectView(R.id.text_view_caption)
    TextView mCaptionTextView;

    private boolean mWidgetsVisible = true;

    private GagItem mGagItem;

    private PhotoViewAttacher mAttacher;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_gag_item);
        ButterKnife.inject(this);

        mAttacher = new PhotoViewAttacher(imageView);

        mAttacher.setOnViewTapListener(this);

        handleIntent(getIntent());

        String url = mGagItem.getImageUrlLarge();
        VolleySingleton.getInstance(this).getImageLoader().get(url,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                        Bitmap bitmap = null;
                        if((bitmap = imageContainer.getBitmap()) != null){
                            imageView.setImageBitmap(bitmap);
                            mAttacher.update();
                            mProgressBar.setVisibility(View.GONE);
                            Log.d(TAG, "onResponse and bitmap not null, isImmediate: " + isImmediate + " bitmap size: " + bitmap.getWidth() + " , " + bitmap.getHeight());
                        } else {
                            Log.d(TAG, "onResponse and bitmap is null, isImmediate: " + isImmediate);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, "onErrorResponse");
                        imageView.setImageResource(R.drawable.empty_photo);
                        mAttacher.update();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }, MAX_WIDTH, MAX_HEIGHT);


        String caption = mGagItem.getCaption();
        mCaptionTextView.setText(caption);
    }

    private void toggleWidgetsVisibility() {
        if (mWidgetsVisible) {
            getActionBar().hide();
            mCaptionTextView.setVisibility(View.GONE);
            mWidgetsVisible = false;
        } else {
            getActionBar().show();
            mCaptionTextView.setVisibility(View.VISIBLE);
            mWidgetsVisible = true;
        }
    }

    private void handleIntent(Intent intent) {
        mGagItem = (GagItem) intent.getSerializableExtra(EXTRA_GAG_ITEM);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gag_item, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider provider = (ShareActionProvider) shareItem.getActionProvider();
        provider.setShareIntent(createShareIntent());

        return true;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String text = mGagItem.getCaption() + "\n" + mGagItem.getImageUrlLarge() + "\nvia @" + getString(R.string.app_name);
        intent.putExtra(Intent.EXTRA_TEXT, text);

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
        if (id == R.id.action_download) {
            downloadImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadImage() {
        Intent intent = new Intent(this, SaveImageFileService.class);
        intent.putExtra(SaveImageFileService.EXTRA_GAGITEM, mGagItem);
        startService(intent);
    }



    @Override
    public void onViewTap(View view, float v, float v2) {
        toggleWidgetsVisibility();
    }

}
