package com.example.shakecaine.documentationtraining;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FirstActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.shakecaine.documentationtraining.MESSAGE";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int CONTENT_REQUEST=1337;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static final String PACKAGE_NAME = "com.example.shakecaine.documentationtraining";
    public String DATA_PATH = "";

    public static final String lang = "eng";
    private static final String TAG = "SimpleAndroidOCR";

    protected static final String PHOTO_TAKEN = "photo_taken";

    protected Button _button;
    // protected ImageView _image;
    protected TextView _field;
    protected String _path;
    protected boolean _taken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DATA_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/Assets/";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        /*SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("fuck me", 2);
        editor.commit();*/

        int defaultValue = 0;
        long def = sharedPref.getInt("fuck me",defaultValue);

        TextView editText = ( TextView) findViewById(R.id.edit_message);
        if(def == 1)
            editText.setText("fuck");
        else if (def == 2){
            editText.setText("fuck me");
        }

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on internalstorage failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on internal storage");
                }
            } else {
                Log.v(TAG, "Directory " + path + " EXISTS");
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        _path = new File(getFilesDir(), "Assets/ocr.jpg").getAbsolutePath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first, menu);
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

    public void sendMessage(View view){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
        }

        /*Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);*/
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check if data is returned by camera
        if(data != null) {
            Bundle extras = data.getExtras();
            Bitmap baseBitmap = (Bitmap) extras.get("data");

            baseBitmap = toGrayscale(baseBitmap);

            Bitmap imageBitmap = Bitmap.createScaledBitmap(baseBitmap, (int) (baseBitmap.getWidth() * 2), (int) (baseBitmap.getHeight() * 2), true);

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(_path);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ImageView mImageView = (ImageView) findViewById(R.id.imageView);
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();*/

                File file = new File(_path);
                if (!file.exists())
                    Log.d(TAG, "FUCK YOU");
                mImageView.setImageBitmap(imageBitmap);

                _taken = true;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;


                Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

                try {
                    ExifInterface exif = new ExifInterface(_path);
                    int exifOrientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    Log.v(TAG, "Orient: " + exifOrientation);

                    int rotate = 0;

                    switch (exifOrientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                    }

                    Log.v(TAG, "Rotation: " + rotate);

                    if (rotate != 0) {

                        // Getting width & height of the given image.
                        int w = bitmap.getWidth();
                        int h = bitmap.getHeight();

                        // Setting pre rotate
                        Matrix mtx = new Matrix();
                        mtx.preRotate(rotate);

                        // Rotating Bitmap
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                    }

                    // Convert to ARGB_8888, required by tess
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                } catch (IOException e) {
                    Log.e(TAG, "Couldn't correct orientation: " + e.toString());
                }

                // _image.setImageBitmap( bitmap );

                Log.v(TAG, "Before baseApi");
                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.setDebug(true);
                baseApi.init(DATA_PATH, lang);
                baseApi.setImage(bitmap);
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);

                String recognizedText = baseApi.getUTF8Text();

                baseApi.end();

                // You now have the text in recognizedText var, you can do anything with it.
                // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
                // so that garbage doesn't make it to the display.

                Log.v(TAG, "OCRED TEXT: " + recognizedText);

                if (lang.equalsIgnoreCase("eng")) {
                    recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
                }

                recognizedText = recognizedText.trim();

                TextView editText = (TextView) findViewById(R.id.edit_message);
                if (recognizedText.length() != 0) {
                    editText.setText(recognizedText);
                }

                // Cycle done.
            }
        }
    }
}
