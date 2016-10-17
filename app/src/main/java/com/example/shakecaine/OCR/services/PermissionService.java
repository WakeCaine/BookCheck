package com.example.shakecaine.OCR.services;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by Shakecaine on 2016-07-26.
 */
public class PermissionService {
    public static TessBaseAPI tessBaseAPI;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int CONTENT_REQUEST = 1337;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        boolean grantedPermissions = true;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        if(permission != ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            grantedPermissions = false;
        }

        permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
        if(permission != ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)){
            grantedPermissions = false;
        }


        if(!grantedPermissions){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finish();
            Toast.makeText(activity.getApplicationContext(),"Permissions must be accepted to run an app!", Toast.LENGTH_LONG).show();
        }
    }
}
