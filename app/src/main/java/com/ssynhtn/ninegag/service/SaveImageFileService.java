package com.ssynhtn.ninegag.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.ssynhtn.ninegag.data.GagItem;
import com.ssynhtn.ninegag.volley.VolleySingleton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ssynhtn on 12/4/2014.
 */
public class SaveImageFileService extends Service {
    private static final String TAG = SaveImageFileService.class.getSimpleName();

    public static final String EXTRA_GAGITEM = "extra_gagitem";

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        GagItem item = (GagItem) intent.getSerializableExtra(EXTRA_GAGITEM);
        final String filename = item.getCaption() + ".png";
        final String url = item.getImageUrlLarge();

        final ImageLoader imageLoader = VolleySingleton.getInstance(this).getImageLoader();
        showToast("start downloading");

        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer imageContainer, boolean b) {
                if(imageContainer.getBitmap() != null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveBitmapToDisk(imageContainer.getBitmap(), filename);
                            stopSelf(startId);
                        }
                    }).start();
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showToast("failed to download image...");
            }
        });

        return Service.START_NOT_STICKY;
    }


    private void saveBitmapToDisk(Bitmap bitmap, String filename) {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picturesDir.mkdirs();     // make sure the dir is there
        File file = new File(picturesDir, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e){
            Log.e(TAG, "fail: " + e);
            showToast("failed to save file");
        }

        // notify media scanner
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE , Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
        showToast("download success");
    }


    private void showToast(final String message){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SaveImageFileService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
