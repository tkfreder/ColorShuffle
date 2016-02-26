package com.tinakit.colorshuffle;

import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Tina on 2/25/2016.
 */
public class ColorTask  extends AsyncTask<Bitmap, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        return BitmapUtils.shiftRGB(params[0]);
    }

    @Override protected void onPostExecute(Bitmap result) {
        EventBus.getInstance().post(new ColorTaskResultEvent(result));
    }
}
