package com.lijukay.famecrew.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.elevation.SurfaceColors;
import com.lijukay.famecrew.R;

public class StartActivity extends AppCompatActivity {

    private SharedPreferences firstStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstStart = getSharedPreferences("firstStart", 0);

        if (!firstStart.getBoolean("firstStart", true)){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_open_file);

        int color = SurfaceColors.SURFACE_2.getColor(this);
        MaterialToolbar materialToolbar = findViewById(R.id.materialToolbar);
        RelativeLayout relativeLayout = findViewById(R.id.buttonLayout);
        MaterialButton nextCreate = findViewById(R.id.createNext);

        materialToolbar.setBackgroundColor(color);
        getWindow().setNavigationBarColor(color);
        getWindow().setStatusBarColor(color);
        relativeLayout.setBackgroundColor(color);

        nextCreate.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            firstStart.edit().putBoolean("firstStart", false).apply();
            this.finish();
        });
    }
}