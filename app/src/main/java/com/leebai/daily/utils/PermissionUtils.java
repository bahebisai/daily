package com.leebai.daily.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by swd1 on 17-11-23.
 */

public class PermissionUtils {
    public static final int REQUEST_PERMISSON_STORAGE = 1;
    public static final int REQUEST_PERMISSON_CAMERA = 2;
    public static final int REQUEST_PERMISSON_MICPHONE = 3;

    public static boolean isCameraPermissionGranted(Activity activity) {
        int checkSelfReadStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (checkSelfReadStoragePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSON_CAMERA);
            return false;
        }
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        int checkSelfReadStoragePermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkSelfReadStoragePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSON_STORAGE);
            return false;
        }
    }

    public static boolean isMicPhonePermissionGranted(Activity activity) {
        int checkSelfReadStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (checkSelfReadStoragePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSON_MICPHONE);
            return false;
        }
    }


}
