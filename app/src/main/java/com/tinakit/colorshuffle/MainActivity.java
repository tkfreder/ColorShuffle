package com.tinakit.colorshuffle;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_GALLERY_IMAGE = 1;
    private static int MAX_IMAGES_CACHED = 3;

    protected Button chooseImageButton;
    protected Button shuffleImageButton;
    protected ImageView imageRgb;
    private Bitmap mBitmap;
    private String mFilePath;
    private LruCache<String, Bitmap> mMemoryCache;
    private int shuffleIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // wire up UI components
        chooseImageButton = (Button)findViewById(R.id.chooseImageButton);
        shuffleImageButton = (Button)findViewById(R.id.shuffleImageButton);
        imageRgb = (ImageView)findViewById(R.id.imageRgb);

        // set up memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8 of available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

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

        // subscribe activity to event bus
        EventBus.getInstance().register(this);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String imageKey, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            new ColorTask().execute(mBitmap);
        }
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

                // scale image
                new ScaleTask().execute(mFilePath, imageRgb.getWidth(), imageRgb.getHeight());
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
                shuffleIndex++;
                loadBitmap(mFilePath + String.valueOf(shuffleIndex < MAX_IMAGES_CACHED ? shuffleIndex : shuffleIndex%MAX_IMAGES_CACHED), imageRgb);
            }
        });

    }

    @Override
    protected void onDestroy() {
        EventBus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onScaleTaskResult(ScaleTaskResultEvent event) {
        mBitmap = event.getResult();
        // load bitmap
        imageRgb.setImageBitmap(mBitmap);
        // wait one second
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // save first image
        addBitmapToMemoryCache(mFilePath + String.valueOf(shuffleIndex), mBitmap);
        // increment image index
        shuffleIndex++;
        // load image from cache or load a new one
        loadBitmap(mFilePath + String.valueOf(shuffleIndex < MAX_IMAGES_CACHED ? shuffleIndex : shuffleIndex%MAX_IMAGES_CACHED), imageRgb);
    }

    @Subscribe
    public void onColorTaskResult(ColorTaskResultEvent event) {
        mBitmap = event.getResult();
        addBitmapToMemoryCache(mFilePath + String.valueOf(shuffleIndex), mBitmap);
        imageRgb.setImageBitmap(mBitmap);
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
