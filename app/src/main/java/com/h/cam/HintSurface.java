package com.h.cam;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
}
