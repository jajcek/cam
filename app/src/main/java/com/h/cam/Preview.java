package com.h.cam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preview implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {

    private static final int COMP_WIDTH = 320;
    private static final int COMP_HEIGHT = 180;

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private HintSurface hintSurface;
    private Mat patternImage;
    private ImageView imageView;
    private Activity activity;

    public Preview(SurfaceView previewSurface, SurfaceView hintSurfaceView, Activity a, ImageView imageView) {
        surfaceHolder = previewSurface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.activity = a;
        this.imageView = imageView;

        hintSurface = new HintSurface(hintSurfaceView);
    }

    public Camera getCamera() {
        return camera;
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
        params.setPictureSize(1280, 720);
            params.setPreviewSize(1280, 720);
        camera.setParameters(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            newOpenCamera();

            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }
    private CameraHandlerThread mThread = null;

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Mat mat = new Mat();
        Bitmap a = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Bitmap bmp32 = a.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(a, mat);

        setPatternImage(mat);

//        final Bitmap fromMat = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat, fromMat);
//
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imageView.setImageBitmap(fromMat);
//            }
//        });

//        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//        System.out.println("pictureFile: " + pictureFile.getAbsolutePath());
//        if (pictureFile == null){
//            Log.d("ERROR", "Error creating media file, check storage permissions");
//            return;
//        }
//
//        try {
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            Log.d("ERROR", "File not found: " + e.getMessage());
//        } catch (IOException e) {
//            Log.d("ERROR", "Error accessing file: " + e.getMessage());
//        }
//
//        scanFile(applicationContext, pictureFile, null);

        camera.startPreview();
    }

    public void changeToHintMode() {
        camera.takePicture(null, null, this);
    }

    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setUpCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            }
            catch (InterruptedException e) {
                System.out.println("wait was interrupted");
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        System.out.println("preview surface destroyed");
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(this);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (patternImage == null) return;

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        Mat yuv = new Mat(height+height/2,width, CvType.CV_8UC1);
        Mat previewImage = new Mat();

        yuv.put(0, 0, data);
        Imgproc.cvtColor(yuv, previewImage, Imgproc.COLOR_YUV2RGBA_NV21);

        Mat previewImageResized = new Mat();
        Size sz = new Size(COMP_WIDTH,COMP_HEIGHT);
        Imgproc.resize( previewImage, previewImageResized, sz );

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // First photo
        Mat descriptors1 = new Mat();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

        detector.detect(previewImageResized, keypoints1);
        descriptor.compute(previewImageResized, keypoints1, descriptors1);

        // Second photo
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

        detector.detect(patternImage, keypoints2);
        descriptor.compute(patternImage, keypoints2, descriptors2);

        MatOfDMatch matches = new MatOfDMatch();
        MatOfDMatch filteredMatches = new MatOfDMatch();
        matcher.match(descriptors1, descriptors2, matches);

        // Linking
        Scalar RED = new Scalar(255,0,0);
        Scalar GREEN = new Scalar(0,255,0);

        // Matching
        List<DMatch> matchesList = matches.toList();
        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for(int i = 0;i < matchesList.size(); i++){
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if ( dist > max_dist)
                max_dist = dist;
        }

        List<KeyPoint> keyPoints = keypoints1.toList();
        List<KeyPoint> keyPoints1 = keypoints2.toList();

        List<Point> points = new ArrayList<>();
        for(int i = 0;i < matchesList.size(); i++){
            if (matchesList.get(i).distance <= min_dist) {
                Point p1 = keyPoints.get(matchesList.get(i).queryIdx).pt;
                Point p2 = keyPoints1.get(matchesList.get(i).trainIdx).pt;
                Point p3 = new Point(p1.x - p2.x, p1.y - p2.y);
                points.add(p3);
            }
        }

        Point a = new Point(0, 0);
        for (Point point : points) {
            a = new Point(a.x + point.x, a.y + point.y);
        }
        Point p3 = new Point((a.x / points.size())*(1280/COMP_WIDTH), (a.y / points.size())*(720/COMP_HEIGHT));
        System.out.println(p3);
        System.out.println("keypoints: " + points.size());

        hintSurface.drawHint(p3);
    }

    public void release() {
        System.out.println("relace in preview");
        patternImage = null;

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void setPatternImage(Mat patternImage) {
        Mat resizeimage = new Mat();
        Size sz = new Size(COMP_WIDTH, COMP_HEIGHT);
        Imgproc.resize( patternImage, resizeimage, sz );
        this.patternImage = resizeimage;
    }
}
