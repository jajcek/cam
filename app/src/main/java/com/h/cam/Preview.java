package com.h.cam;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private Activity a;

    public Preview(Activity context) {
        super(context);
a = context;
        setUpCamera();
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setUpCamera() {
        camera = Camera.open(0);
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        System.out.println("preview sizes: " + previewSizes);
        for (Camera.Size a : previewSizes) {
            System.out.println(a.width + "x" + a.height + "(" + ((float)a.height / a.width) + ")");
        }

        System.out.println("picture sizes: " + previewSizes);
        for (Camera.Size a : pictureSizes) {
            System.out.println(a.width + "x" + a.height + "(" + ((float)a.height / a.width) + ")");
        }

        System.out.println("preview size: " + params.getPreviewSize().width + "x" + params.getPreviewSize().height);
        System.out.println("picture size: " + params.getPictureSize().width + "x" + params.getPictureSize().height);

//        params.setPictureSize(4128, 2322);
//            params.setPreviewSize(640, 480);
        camera.setParameters(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

//            setCameraDisplayOrientation(a,0,camera);
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
            camera.startPreview();

        } catch (Exception e){
            System.out.println("fail");
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
