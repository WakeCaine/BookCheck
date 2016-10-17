package com.example.shakecaine.bookcheck;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shakecaine.OCR.services.CameraService;
import com.example.shakecaine.OCR.services.PermissionService;
import com.example.shakecaine.OCR.services.TextDetectService;
import com.example.shakecaine.OCR.services.TextExtractService;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
Copy of a class that i dont want to loose
 */

public class FirstTestActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.shakecaine.bookcheck.MESSAGE";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int CONTENT_REQUEST = 1337;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static final String PACKAGE_NAME = "com.example.shakecaine.bookcheck";
    public String DATA_PATH = "";

    public static final String lang = "eng";
    private static final String TAG = "SimpleAndroidOCR";

    protected static final String PHOTO_TAKEN = "photo_taken";

    protected Button _button;
    protected ImageView imageView;
    boolean isImageFitToScreen;
    protected TextView _field;
    protected String _path;
    protected boolean _taken;

    ProgressDialog mDialog;

    private Uri fileUri;

    protected TextDetectService textDetectServiceImpl;
    protected TextExtractService textExtractService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textExtractService = new TextExtractService();
        textDetectServiceImpl = new TextDetectService();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        DATA_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/Assets/";
        setContentView(R.layout.activity_first);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

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

        fileUri = CameraService.getOutputMediaFileUri(1);
        _path = fileUri.getPath();

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageFitToScreen) {
                    isImageFitToScreen = false;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setAdjustViewBounds(true);
                } else {
                    isImageFitToScreen = true;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching for Book");
        mDialog.setCancelable(false);
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

    public void sendMessage(View view) {
        PermissionService.tessBaseAPI.init(DATA_PATH, lang);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

        /*Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);*/
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
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

    /*public static void saveBitmapToJpg(Bitmap bitmap, File file, int dpi) throws IOException {
        ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageByteArray);
        byte[] imageData = imageByteArray.toByteArray();

        setDpi(imageData, dpi);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(imageData);
        fileOutputStream.close();
    }

    private static void setDpi(byte[] imageData, int dpi) {
        imageData[13] = 1;
        imageData[14] = (byte) (dpi >> 8);
        imageData[15] = (byte) (dpi & 0xff);
        imageData[16] = (byte) (dpi >> 8);
        imageData[17] = (byte) (dpi & 0xff);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mDialog.show();
        AnalyzeClass analyzeClass = new AnalyzeClass();
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        analyzeClass.analyzeText(_path, mDialog, requestCode,resultCode, this, textExtractService);
    }

    @Override protected void onDestroy(){
        PermissionService.tessBaseAPI.end();
        super.onDestroy();
    }

    protected void setValues(Bitmap bitmapToSet, String textToSet){
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(bitmapToSet);
        TextView editText = (TextView) findViewById(R.id.edit_message);
        if (textToSet.length() != 0) {
            editText.setText(textToSet);
        }
    }

    class AnalyzeClass extends AsyncTask<String, Void, List<List<String>>> {
        ProgressDialog dialog = null;
        int requestCode, resultCode;
        FirstActivity activity;
        Bitmap bitmapHere;
        String textHere;
        TextExtractService textExtractService;

        @Override protected List<List<String>> doInBackground(String... params) {
            String _path = params[0];
            ///////////////////////////////////////////////////////////////////////
            BitmapFactory.Options optionss = new BitmapFactory.Options();
            optionss.inSampleSize = 3;
            optionss.inScreenDensity = 300;
            optionss.inTargetDensity = 300;
            optionss.inPreferQualityOverSpeed = true;

            Bitmap captureBmp = BitmapFactory.decodeFile(_path, optionss);

            if (captureBmp != null) {

                Log.d(this.getClass().getSimpleName(), "SIZE OF BITMAP FROM CAMERA: HEIGHT = " + captureBmp.getHeight() + ", WIDTH = " + captureBmp.getWidth());
                Bitmap imageBitmap = captureBmp; //toGrayscale(captureBmp);

                imageBitmap = CameraService.fixOrientationAndSave(imageBitmap, _path);

                //Test OPENCV
                String inputFileName = "sma";
                String outputExtension = "jpg";

                Log.d(this.getClass().getSimpleName(), "loading " + _path + "...");
                Mat image = Imgcodecs.imread(_path, 0);
                Mat rgb = new Mat();
                Mat small = new Mat();
                rgb = image;
                small = rgb;

                Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
                Photo.fastNlMeansDenoising(image, im_canny);
                image = im_canny;
                Photo.fastNlMeansDenoising(small, im_canny);
                small = im_canny;

                List<Mat> matList = textDetectServiceImpl.rectangleText(small,image);
                rgb = matList.remove(matList.size() - 1);

                String cannyFilename = getFilesDir() + File.separator + inputFileName + "." + outputExtension;
                Imgcodecs.imwrite(cannyFilename, rgb);

                int loop = 1;
                for(Mat m : matList)
                {
                    if(loop == matList.size())
                        break;
                    cannyFilename = getFilesDir() + File.separator + inputFileName + loop + "." + outputExtension;
                    Imgcodecs.imwrite(cannyFilename, m);
                    loop += 1;
                }
                //-------------------------------------------------------------------------------------


                //Imgcodecs.imread(_path, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                Log.d(this.getClass().getSimpleName(), "width of  file" + image.width());
                // if width is 0 then it did not read your image.

                // for the canny edge detection algorithm, play with these to see different results
                int threshold1 = 300;
                int threshold2 = 300;

                //Save to gallery
                optionss.inSampleSize = 1;
                cannyFilename = getFilesDir() + File.separator + inputFileName + "." + outputExtension;
                Bitmap bitmap123 = BitmapFactory.decodeFile(cannyFilename, optionss);
                bitmap123 = Bitmap.createScaledBitmap(bitmap123, (bitmap123.getWidth() / 2), (bitmap123.getHeight() / 2), true);

                if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();*/

                    File file = new File(_path);
                    if (!file.exists())
                        Log.d(TAG, "FUCK YOU");
                    //mImageView.setImageBitmap(imageBitmap);

                    _taken = true;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inScreenDensity = 300;
                    options.inTargetDensity = 300;
                    options.inPreferQualityOverSpeed = true;


                    Bitmap bitmap = BitmapFactory.decodeFile(_path, options);
                    Log.d(TAG, "Scaled down bitmap size HEIGHT = " + bitmap.getHeight() + ", WIDTH = " + bitmap.getWidth());

                    Log.v(TAG, "Before baseApi");
                    PermissionService.tessBaseAPI.setImage(bitmap);

                    PermissionService.tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

                    String recognizedText = PermissionService.tessBaseAPI.getUTF8Text();

                    if (lang.equalsIgnoreCase("eng")) {
                        recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
                    }

                    recognizedText = recognizedText.trim();

                    Log.v(TAG, "OCRED TEXT: " + recognizedText);

                    // Cycle done.
                    bitmapHere = bitmap123;
                    textHere = recognizedText;
                }

                loop = 1;
                for(Mat m : matList)
                {
                    if(loop == matList.size())
                        break;
                    cannyFilename = getFilesDir() + File.separator + inputFileName + loop + "." + outputExtension;
                    textDetectServiceImpl.detectTextRectangle(cannyFilename, DATA_PATH, lang, textExtractService);
                    loop += 1;
                }
            }
            //////////////////////////////////////////////////////////////////////


            return new ArrayList<>();
        }

        public void analyzeText(String _path, ProgressDialog mDialog, int requestCode, int resultCode, Activity activity, TextExtractService textExtractService){
            this.dialog = mDialog;
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.activity = (FirstActivity) activity;
            this.textExtractService = textExtractService;
            this.execute(_path);
        }

        protected void onPostExecute(List<List<String>> list){
            this.activity.setValues(bitmapHere,textHere);
            textExtractService.extractWords(textHere);
            Log.d("WORDS:", textExtractService.getMainWords(false));
            mDialog.dismiss();
        }
    }
}
