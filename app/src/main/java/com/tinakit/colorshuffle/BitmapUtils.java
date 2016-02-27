package com.tinakit.colorshuffle;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by Tina on 2/25/2016.
 */
public class BitmapUtils {

    //**********************************************************************************************
    //  calculateInSampleSize
    //**********************************************************************************************
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // According to docs: the decoder uses a final value based on powers of 2, any other value will be rounded down to the nearest power of 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //**********************************************************************************************
    //  decodeSampledBitmapFromFile
    //**********************************************************************************************
    public static Bitmap decodeSampledBitmapFromFile(String filePath,int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // set to mutable
        options.inMutable = true;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //**********************************************************************************************
    //  decodeSampledBitmapFromResource
    //**********************************************************************************************
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // set to mutable
        options.inMutable = true;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    //**********************************************************************************************
    //  shiftRGB
    //**********************************************************************************************
    public static Bitmap shiftRGB(Bitmap bitmap){
        // reference: http://stackoverflow.com/questions/20157194/looping-through-bitmap-pixels-to-change-color-of-a-bitmap-in-android
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int colorRGB = bitmap.getPixel(x,y);
                int red = Color.red(colorRGB);
                int green = Color.green(colorRGB);
                int blue = Color.blue(colorRGB);
                // shift rgb values 1 place to the right
                bitmap.setPixel(x, y, Color.rgb(blue, red, green));
            }
        }
        return bitmap;
    }
}
