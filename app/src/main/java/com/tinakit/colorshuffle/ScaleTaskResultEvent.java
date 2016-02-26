package com.tinakit.colorshuffle;

import android.graphics.Bitmap;

/**
 * Created by Tina on 2/25/2016.
 */
public class ScaleTaskResultEvent {
    private Bitmap result;

    public ScaleTaskResultEvent(Bitmap result) {
        this.result = result;
    }

    public Bitmap getResult() {
        return result;
    }
}
