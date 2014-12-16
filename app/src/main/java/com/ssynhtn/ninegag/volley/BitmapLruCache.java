package com.ssynhtn.ninegag.volley;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by ssynhtn on 11/14/2014.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    public BitmapLruCache(){
        super(computeSuitableCacheSize());
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public Bitmap getBitmap(String key) {
        return get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        put(key, bitmap);
    }

    public static int computeSuitableCacheSize(){
        Long maxMemory = Runtime.getRuntime().maxMemory() / 8;
        return maxMemory.intValue();

    }
}
