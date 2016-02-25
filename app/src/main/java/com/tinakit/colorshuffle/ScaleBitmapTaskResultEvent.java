package com.tinakit.colorshuffle;

import android.graphics.Bitmap;

/**
 * Created by Tina on 2/25/2016.
 */
public class ScaleBitmapTaskResultEvent {
    private Bitmap result;

    public ScaleBitmapTaskResultEvent(Bitmap result) {
        this.result = result;
    }

    public Bitmap getResult() {
        return result;
    }
}
