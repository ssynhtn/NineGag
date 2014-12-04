package com.ssynhtn.ninegag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        ImageLoader imageLoader = VolleySingleton.getInstance(this).getImageLoader();
//        imageLoader.getImageListener(null, )


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
        ImageLoader imageLoader = VolleySingleton.getInstance(this).getImageLoader();
        showToast("start downloading");
        imageLoader.get(mGagItem.getImageUrlLarge(), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                Bitmap bm = null;
                if((bm = imageContainer.getBitmap()) != null){
                    try {
                        saveBitmapToDisk(bm);
                        showToast("download success!");
                    } catch (IOException e) {
                        showToast("failed to save file to disk");
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showToast("failed to download image...");
            }
        });
    }

    private void showToast(String s) {
        if(mToast != null){
            mToast.cancel();
        }

        mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mToast.show();
    }

    // currently don't care about overriding user images... which is bad
    private void saveBitmapToDisk(Bitmap bitmap) throws IOException {
//        String filename = String.format(mGagItem.hashCode() + ".png");
        String filename = mGagItem.getCaption() + ".png";
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picturesDir.mkdirs();     // make sure the dir is there
        File file = new File(picturesDir, filename);
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();

        // notify media scanner
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE , Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);

    }


    @Override
    public void onViewTap(View view, float v, float v2) {
        toggleWidgetsVisibility();
    }
}
