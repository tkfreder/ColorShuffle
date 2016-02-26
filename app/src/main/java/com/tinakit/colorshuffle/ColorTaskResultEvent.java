package com.tinakit.colorshuffle;

import android.graphics.Bitmap;

/**
 * Created by Tina on 2/25/2016.
 */
public class ColorTaskResultEvent  {
    private Bitmap result;

    public ColorTaskResultEvent(Bitmap result) {
        this.result = result;
    }

    public Bitmap getResult() {
        return result;
    }
}