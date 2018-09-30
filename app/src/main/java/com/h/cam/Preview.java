package com.h.cam;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public Preview(Context context) {
        super(context);

        setUpCamera();
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setUpCamera() {
        camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        System.out.println("preview sizes: " + previewSizes);
        for (Camera.Size a : previewSizes) {
            System.out.println(a.width + "x" + a.height);
        }
//            params.setPreviewSize(640, 480);
        camera.setParameters(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            System.out.println("wlazlo");
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
// If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        System.out.println("wlazlo2");
        if (surfaceHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
//            camera.setPreviewCallback(this);
            camera.startPreview();

        } catch (Exception e){
            Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void release() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
