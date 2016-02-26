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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_GALLERY_IMAGE = 1;

    protected Button mChooseButton;
    protected Button mShuffleButton;
    protected ImageView mImage;
    protected ProgressBar mProgressBar;
    private Bitmap mBitmap;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // wire up UI components
        mChooseButton = (Button)findViewById(R.id.chooseImageButton);
        mShuffleButton = (Button)findViewById(R.id.shuffleImageButton);
        mImage = (ImageView)findViewById(R.id.imageRgb);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        // set action listeners
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                new ColorTask().execute(mBitmap);            }
        });

        // subscribe activity to event bus
        EventBus.getInstance().register(this);
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
        mBitmap = event.getResult();
        // load bitmap
        mImage.setImageBitmap(mBitmap);
        mProgressBar.setVisibility(View.GONE);
    }

    @Subscribe
    public void onColorTaskResult(ColorTaskResultEvent event) {
        mBitmap = event.getResult();
        mImage.setImageBitmap(mBitmap);
        mProgressBar.setVisibility(View.GONE);
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
