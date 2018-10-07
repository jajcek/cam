package com.h.cam;

import android.content.Context;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import java.util.ArrayList;
import java.util.List;

public class ButtonsOrientationListener extends OrientationEventListener {
    private final int ROTATION_0 = 0;
    private final int ROTATION_90 = 90;
    private final int ROTATION_180 = 180;
    private final int ROTATION_270 = 270;
    private final int ROTATION_360 = 360;
    private int rotation = -1;

    private final int SETUP_DURATION = 0;
    private final int ROTATE_DURATION_MS = 200;
    private final List<View> views = new ArrayList<>();

    public ButtonsOrientationListener(Context context) {
        super(context);
    }

    public void addView(View view) {
        views.add(view);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (rotation == -1) {
            setupOrientation(orientation);
        } else {
            rotate(orientation);
        }
    }

    private void setupOrientation(int orientation) {
        if (isPortrait(orientation)) {
            rotateView(ROTATION_270, ROTATION_270, SETUP_DURATION);
        } else if (isReversePortrait(orientation)) {
            rotateView(ROTATION_90, ROTATION_90, SETUP_DURATION);
        } else if (isReverseLandscape(orientation)) {
            rotateView(ROTATION_180, ROTATION_180, SETUP_DURATION);
        } else if (isLandscape(orientation)) {
            rotateView(ROTATION_0, ROTATION_0, SETUP_DURATION);
        }
    }

    private void rotate(int orientation) {
        if ((isPortrait(orientation)) && rotation != ROTATION_270) {
            rotateView(rotation == ROTATION_0 ? ROTATION_360 : rotation, ROTATION_270, ROTATE_DURATION_MS);
        } else if (isReversePortrait(orientation) && rotation != ROTATION_90) {
            rotateView(rotation, ROTATION_90, ROTATE_DURATION_MS);
        } else if (isReverseLandscape(orientation) && rotation != ROTATION_180) {
            rotateView(rotation, ROTATION_180, ROTATE_DURATION_MS);
        } else if (isLandscape(orientation) && rotation != ROTATION_0) {
            rotateView(rotation, rotation == ROTATION_270 ? ROTATION_360 : ROTATION_0, ROTATE_DURATION_MS);
            rotation = ROTATION_0;
        }
    }

    private boolean isLandscape(int orientation) {
        return orientation > 235 && orientation < 305;
    }

    private boolean isReverseLandscape(int orientation) {
        return orientation > 55 && orientation < 125;
    }

    private boolean isReversePortrait(int orientation) {
        return orientation > 145 && orientation < 215;
    }

    private boolean isPortrait(int orientation) {
        return orientation < 35 || orientation > 325;
    }

    private void rotateView(int from, int to, int duration) {
        rotation = to;

        RotateAnimation animation = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        for (View view : views) {
            view.startAnimation(animation);
        }
    }
}