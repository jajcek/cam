package com.h.cam;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {

    private final Activity activity;
    public static final int PERMISSION_REQUEST_CODE = 1;

    public Permissions(Activity activity) {
        this.activity = activity;
    }

    public boolean hasAllPermissions() {
        return isGranted(Manifest.permission.CAMERA) && isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestNecessaryPermissions() {
        int camera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int writeExtStorage = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (camera != PackageManager.PERMISSION_GRANTED || writeExtStorage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }
}
