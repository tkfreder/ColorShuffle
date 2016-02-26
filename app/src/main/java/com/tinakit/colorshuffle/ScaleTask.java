package com.tinakit.colorshuffle;

import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Tina on 2/25/2016.
 */
public class ScaleTask extends AsyncTask<Object, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Object... params) {
        return BitmapUtils.decodeSampledBitmapFromFile((String)params[0], (int)params[1], (int)params[2]);
    }

    @Override protected void onPostExecute(Bitmap result) {
        EventBus.getInstance().post(new ScaleTaskResultEvent(result));
    }
}
