package com.h.cam;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class FullscreenActivity extends Activity {

    private final Permissions permissions = new Permissions(this);
    private Preview preview;
    private ButtonsOrientationListener buttonsOrientationListener;

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

        buttonsOrientationListener = new ButtonsOrientationListener(this);
        buttonsOrientationListener.addView(findViewById(R.id.imageButton3));
        buttonsOrientationListener.addView(findViewById(R.id.imageButton1));
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
