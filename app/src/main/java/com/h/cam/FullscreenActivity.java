package com.h.cam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class FullscreenActivity extends AppCompatActivity {

    private final Permissions permissions = new Permissions(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissions.requestNecessaryPermissions();

        setContentView(R.layout.activity_fullscreen);

    }
}
