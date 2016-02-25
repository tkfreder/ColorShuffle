package com.tinakit.colorshuffle;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_GALLERY_IMAGE = 1;

    protected Button chooseImageButton;
    protected Button shuffleImageButton;
    protected ImageView imageRgb;
    private Bitmap mBitmap;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // wire up UI components
        chooseImageButton = (Button)findViewById(R.id.chooseImageButton);
        shuffleImageButton = (Button)findViewById(R.id.shuffleImageButton);
        imageRgb = (ImageView)findViewById(R.id.imageRgb);

        // set action listeners
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_GALLERY_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // check if request came from Gallery image picker
            if (requestCode == RESULT_GALLERY_IMAGE && resultCode == RESULT_OK && null != data) {
                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mFilePath = cursor.getString(columnIndex);
                cursor.close();

                // save copy of the decoded scaled down bitmap
                mBitmap = decodeSampledBitmapFromFile(mFilePath, imageRgb.getWidth(), imageRgb.getHeight());
                // load scaled down bitmap into imageview
                imageRgb.setImageBitmap(mBitmap);
            } else {
                Toast.makeText(this, R.string.message_no_image_chosen, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.message_error_choose_image, Toast.LENGTH_LONG)
                    .show();
        }

        shuffleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save bitmap
                mBitmap = shuffleImage(mBitmap);
                // display bitmap
                imageRgb.setImageBitmap(mBitmap);
            }
        });

    }

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

    public static Bitmap decodeSampledBitmapFromFile(String filePath,int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // set to mutable
        options.inMutable = true;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }
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

    public Bitmap shuffleImage(Bitmap bitmap){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
