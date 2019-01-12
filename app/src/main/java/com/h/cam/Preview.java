package com.h.cam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Preview implements SurfaceHolder.Callback, Camera.PreviewCallback, CameraBridgeViewBase.CvCameraViewListener2 {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private HintSurface hintSurface;
    private Mat patternImage;
    private ImageView viewById;

    public Preview(SurfaceView previewSurface, SurfaceView hintSurfaceView, ImageView viewById) {
        setUpCamera();
        surfaceHolder = previewSurface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.viewById = viewById;

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
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        System.out.println("wlazlo");
//        System.out.println(a++);
//        int[] pixels = new int[640*480];
//        decodeYUV420SP(pixels, data, 640, 480);
//        System.out.println("color: " + String.format("#%06X", (0xFFFFFF & pixels[0])));
        if (patternImage == null) return;

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
//        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
//        Rect rect = new Rect(0, 0, width, height);
//        YuvImage yuvimage=new YuvImage(data,ImageFormat.NV21,width,height,null);
//        yuvimage.compressToJpeg(rect, 100, outstr);
//        Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());

        Mat yuv = new Mat(height+height/2,width, CvType.CV_8UC1);//height, width, CvType.CV_8UC1
        Mat previewImage = new Mat();//height, width, CvType.CV_8UC4

        yuv.put(0, 0, data);
        Imgproc.cvtColor(yuv, previewImage, Imgproc.COLOR_YUV2RGBA_NV21);
        // --
        Bitmap fromMat = Bitmap.createBitmap(previewImage.cols(), previewImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(previewImage, fromMat);

        viewById.setImageBitmap(fromMat);
        // --
//        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGR2GRAY);
//        mRgba.put(0, 0, data);

//        Mat previewImage = new Mat();
//        Utils.bitmapToMat(bmp, previewImage);
        Mat previewImageResized = new Mat();
        Size sz = new Size(160,90);
        Imgproc.resize( previewImage, previewImageResized, sz );
//        System.out.println(bmp.getPixel(0, 0));


        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
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

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
//        for(KeyPoint a : keypoints1.toList()) {
//            System.out.println(a);
//        }

        List<Point> points = new ArrayList<>();
        for(int i = 0;i < matchesList.size(); i++){
            if (matchesList.get(i).distance <= (4 * min_dist)) {
//                System.out.println("// ---");
                good_matches.addLast(matchesList.get(i));
                Point p1 = keypoints1.toList().get(matchesList.get(i).queryIdx).pt;
                Point p2 = keypoints2.toList().get(matchesList.get(i).trainIdx).pt;
                Point p3 = new Point(p1.x - p2.x, p1.y - p2.y);
                points.add(p3);
//                System.out.println(p3);
            }
        }

        Point a = new Point(0, 0);
        for (Point point : points) {
            a = new Point(a.x + point.x, a.y + point.y);
        }
        Point p3 = new Point((a.x / points.size())*(1280/160), (a.y / points.size())*(720/90));
        System.out.println(p3);

        hintSurface.drawHint(p3);
//        patternImage = null;
    }

    public void release() {
        patternImage = null;

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void setPatternImage(Mat patternImage) {
        Mat resizeimage = new Mat();
        Size sz = new Size(160,90);
        Imgproc.resize( patternImage, resizeimage, sz );
        this.patternImage = resizeimage;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.out.println("wlazlo");
        return null;
    }
}
