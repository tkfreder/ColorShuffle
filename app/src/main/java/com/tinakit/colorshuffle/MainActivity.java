package com.tinakit.colorshuffle;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_GALLERY_IMAGE = 1;
    private static int MAX_IMAGES_CACHED = 3;

    protected Button mChooseButton;
    protected Button mShuffleButton;
    protected ImageView mImage;
    protected ProgressBar mProgressBar;
    private Bitmap mBitmap;
    private String mFilePath;
    private LruCache<String, Bitmap> mMemoryCache;
    private int shuffleIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // wire up UI components
        mChooseButton = (Button)findViewById(R.id.chooseImageButton);
        mShuffleButton = (Button)findViewById(R.id.shuffleImageButton);
        mImage = (ImageView)findViewById(R.id.imageRgb);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        // set up memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8 of available memory for this memory cache.
        final int cacheSize = maxMemory / MAX_IMAGES_CACHED;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
             protected int sizeOf(String key, Bitmap bitmap) {
             // The cache size will be measured in kilobytes rather than number of items.
             return bitmap.getByteCount() / 1024;
             }
        };

        // set action listeners
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset index
                shuffleIndex = 0;
                // launch Gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_GALLERY_IMAGE);
            }
        });

        mShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                shuffleIndex++;
                int adjustedIndex = shuffleIndex < MAX_IMAGES_CACHED ? shuffleIndex : shuffleIndex%MAX_IMAGES_CACHED;
                loadBitmap(mFilePath + String.valueOf(adjustedIndex), mImage);
                //new ColorTask().execute(mBitmap);
            }
        });

        // subscribe activity to event bus
        EventBus.getInstance().register(this);
    }

    public void onSaveInstanceState(Bundle bundle) {
        if (mBitmap != null){
            bundle.putParcelable("image", mBitmap);
            bundle.putString("filePath", mFilePath);
            bundle.putInt("shuffleIndex", shuffleIndex);
        }
        super.onSaveInstanceState(bundle);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("image")){
            mBitmap = (Bitmap) savedInstanceState.getParcelable("image");
            mFilePath = savedInstanceState.getString("filePath");
            shuffleIndex = savedInstanceState.getInt("shuffleIndex");
            // TODO: this might come in handy for LurCache, specifying the width and height of imageview
            //mBitmap=Bitmap.createScaledBitmap(mBitmap,mImage.getWidth(),mImage.getHeight(), true);
            mImage.setImageBitmap(mBitmap);
            mShuffleButton.setEnabled(true);
        }
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
            mProgressBar.setVisibility(View.GONE);
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
                mFilePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                cursor.close();

                mProgressBar.setVisibility(View.VISIBLE);
                mShuffleButton.setEnabled(true);
                // scale image
                new ScaleTask().execute(mFilePath, mImage.getWidth(), mImage.getHeight());
            } else {
                mShuffleButton.setEnabled(false);
                Toast.makeText(this, R.string.message_no_image_chosen, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.message_error_choose_image, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onScaleTaskResult(ScaleTaskResultEvent event) {
        Bitmap bitmap = event.getResult();
        // load bitmap
        mImage.setImageBitmap(bitmap);
        mProgressBar.setVisibility(View.GONE);
        // save first image
        addBitmapToMemoryCache(mFilePath + String.valueOf(shuffleIndex), bitmap);
        // save current bitmap
        mBitmap = Bitmap.createBitmap(bitmap);
    }

    @Subscribe
    public void onColorTaskResult(ColorTaskResultEvent event) {
        Bitmap bitmap = event.getResult();
        addBitmapToMemoryCache(mFilePath + String.valueOf(shuffleIndex), bitmap);
        mImage.setImageBitmap(mBitmap);
        mProgressBar.setVisibility(View.GONE);
        //save current bitmap
        mBitmap = Bitmap.createBitmap(bitmap);
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
