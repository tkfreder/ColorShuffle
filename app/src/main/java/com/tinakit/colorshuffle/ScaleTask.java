package com.tinakit.colorshuffle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;

import java.io.IOException;

/**
 * Created by Tina on 2/25/2016.
 */
public class ScaleTask extends AsyncTask<Object, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Object... params) {
        String filePath = (String)params[0];
        int width = (int)params[1];
        int height = (int)params[2];

        return Bitmap.createBitmap(BitmapUtils.decodeSampledBitmapFromFile(filePath, width, height));
    }

    @Override protected void onPostExecute(Bitmap result) {
        EventBus.getInstance().post(new ScaleTaskResultEvent(result));
    }
}
