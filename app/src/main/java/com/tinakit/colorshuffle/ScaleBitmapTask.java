package com.tinakit.colorshuffle;

import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Tina on 2/25/2016.
 */
public class ScaleBitmapTask extends AsyncTask<ScaleBitmapTaskParams, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(ScaleBitmapTaskParams... params) {
        return BitmapUtils.decodeSampledBitmapFromFile(params[0].mFilePath, params[0].mImageWidth, params[0].mImageHeight);
    }

    @Override protected void onPostExecute(Bitmap result) {
        EventBus.getInstance().post(new ScaleBitmapTaskResultEvent(result));
    }


}
