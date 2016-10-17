package com.example.shakecaine.bookcheck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shakecaine.OCR.services.CameraService;
import com.example.shakecaine.OCR.services.PermissionService;
import com.example.shakecaine.bookcheck.main.BookActivity;
import com.example.shakecaine.bookcheck.main.FavListActivity;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.w3c.dom.Text;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.Getter;

@Getter
public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    static final String TAG = "StartActivity";
    public static final String lang = "eng";

    //UI management
    ImageView searchImage;
    ImageView favouriteListImage;
    TextView searchTextView;

    //Text detection prepare
    Uri fileUri;
    String _path;
    String DATA_PATH = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        PermissionService.tessBaseAPI = new TessBaseAPI();
        //set picture data folder
        DATA_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/Assets/";
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

        //fileUri = CameraService.getOutputMediaFileUri(1);
        //_path = fileUri.getPath();

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        PermissionService.tessBaseAPI.setDebug(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        searchImage = (ImageView) findViewById(R.id.buttonSearch);
        favouriteListImage = (ImageView) findViewById(R.id.buttonFav);
        searchTextView = (TextView) findViewById(R.id.textViewSearch);
        searchImage.setOnClickListener(this);
        favouriteListImage.setOnClickListener(this);
        searchTextView.setOnClickListener(this);

        PermissionService.verifyStoragePermissions(this);
    }

    @Override public void onClick(View v) {
        if(v instanceof ImageView){
            if(v == searchImage){
                Toast.makeText(getApplicationContext(),"SEARCH", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(getApplicationContext(), BookActivity.class);
                startActivity(mainIntent);
            } else if(v == favouriteListImage) {
                Toast.makeText(getApplicationContext(), "FAV LIST", Toast.LENGTH_SHORT).show();
                Intent favListIntent = new Intent(getApplicationContext(), FavListActivity.class);
                startActivity(favListIntent);
            }
        } else if(v instanceof TextView){
            if(v == searchTextView) {
                Toast.makeText(getApplicationContext(), "DETECT TEXT", Toast.LENGTH_SHORT).show();
                Intent searchIntent = new Intent(getApplicationContext(), FirstActivity.class);
                startActivity(searchIntent);
            }
        }
    }

    @Override public void onDestroy(){
        PermissionService.tessBaseAPI.end();
        super.onDestroy();
    }
}
