package com.h.cam;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    static {
        OpenCVLoader.initDebug();
    }

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
        preview = new Preview((SurfaceView) findViewById(R.id.previewSurface));

        SurfaceView a = (SurfaceView) findViewById(R.id.hintSurface);


        SurfaceHolder h = a.getHolder();
        h.setFormat(PixelFormat.TRANSPARENT);
        h.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(12);
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                Path p = new Path();
                p.moveTo(0, 0);
                Canvas c = holder.lockCanvas();
                c.save();
                p.lineTo(200, 200);
                c.drawRect(0, 0, 200, 200, paint);
                c.restore();
                holder.unlockCanvasAndPost(c);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        prepareButtons(preview.getCamera());
    }

    private void prepareButtons(Camera camera) {
        buttonsOrientationListener = new ButtonsOrientationListener(this);
        buttonsOrientationListener.addView(findViewById(R.id.imageButton3));

        View takePhotoButton = findViewById(R.id.imageButton1);
        takePhotoButton.setOnClickListener(new PhotoTaker(getApplicationContext(), camera, (ImageView) findViewById(R.id.imageButton123)));
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
