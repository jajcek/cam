package com.h.cam;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public Preview(Activity context) {
        super(context);

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
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        System.out.println("wlazlo");
//        System.out.println(a++);
//        int[] pixels = new int[1280*720];
//        decodeYUV420SP(pixels, data, 1280, 720);
//        System.out.println("color: " + String.format("#%06X", (0xFFFFFF & pixels[0])));
    }

    //Method from Ketai project! Not mine! See below...
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public void release() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
