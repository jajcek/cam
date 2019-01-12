package com.h.cam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class PhotoTaker implements View.OnClickListener, Camera.PictureCallback {
    private Preview preview;
    private Context applicationContext;
    private ImageView imageView;

    public PhotoTaker(Context applicationContext, Preview preview, ImageView imageView) {
        this.applicationContext = applicationContext;
        this.preview = preview;
        this.imageView = imageView;
    }

    @Override
    public void onClick(View v) {
        preview.getCamera().takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Mat mat = new Mat();
        Bitmap a = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Bitmap bmp32 = a.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(a, mat);

        preview.setPatternImage(mat);

        Bitmap fromMat = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, fromMat);

        imageView.setImageBitmap(fromMat);
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

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        System.out.println("storage: " + mediaStorageDir.getAbsolutePath());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void scanFile(Context ctxt, File f, String mimeType) {
        MediaScannerConnection.scanFile(ctxt, new String[] {f.getAbsolutePath()}, new String[] {mimeType}, null);
    }
}
