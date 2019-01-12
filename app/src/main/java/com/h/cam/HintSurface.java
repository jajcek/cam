package com.h.cam;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.core.Point;

public class HintSurface implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;

    public HintSurface(SurfaceView surfaceView) {
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(12);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Canvas c = holder.lockCanvas();
        c.save();
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

    public void drawHint(Point p3) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(12);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Canvas c = surfaceHolder.lockCanvas();
        c.save();
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        c.drawRect((float) p3.x, (float) p3.y, (float) p3.x + 1280, (float) p3.y + 720, paint);
        c.restore();
        surfaceHolder.unlockCanvasAndPost(c);
//        Imgproc.rectangle(outputImg, p3, new Point(p3.x + img2.width(), p3.y + img2.height()),
//        new Scalar(0, 0, 0), 2, 8, 0);
    }
}
