package com.h.cam;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

public class FullscreenActivity extends Activity {

    private final Permissions permissions = new Permissions(this);
    private Preview preview;
    private ButtonsOrientationListener buttonsOrientationListener;

    static{OpenCVLoader.initDebug();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        if (permissions.hasAllPermissions()) {
            runPreview();
        } else {
            permissions.requestNecessaryPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkIfAllGranted(grantResults)) {
            try {
                runPreview();
            } catch (Exception e) {
                Toast.makeText(this, "Preview could not be initialized properly.", Toast.LENGTH_LONG).show();
                Log.e("Error", "Error when initializing preview: " + e.getMessage());
            }
        } else {
            Toast.makeText(this, "Not all permissions has been granted.", Toast.LENGTH_LONG).show();
        }
    }

    private void runPreview() {
        FrameLayout previewFrame = findViewById(R.id.preview);
        preview = new Preview(this);
        previewFrame.addView(preview);

        prepareButtons();

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        System.out.println(detector);
    }

    private void prepareButtons() {
        buttonsOrientationListener = new ButtonsOrientationListener(this);
        buttonsOrientationListener.addView(findViewById(R.id.imageButton3));

        View takePhotoButton = findViewById(R.id.imageButton1);
        takePhotoButton.setOnClickListener(new PhotoTaker());
        buttonsOrientationListener.addView(takePhotoButton);

        buttonsOrientationListener.enable();
    }

    private boolean checkIfAllGranted(int[] grantResults) {
        for (int grant : grantResults) {
            if (grant == -1) return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        buttonsOrientationListener.disable();
        if (preview != null) {
            preview.release();
            preview = null;
        }
        super.onPause();
    }
}
