package com.tinakit.colorshuffle;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Tina on 2/25/2016.
 */
public class ImageCache extends LruCache<String,Bitmap> {

    public ImageCache(int maxSize){
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return super.sizeOf(key, value);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
    }
}
