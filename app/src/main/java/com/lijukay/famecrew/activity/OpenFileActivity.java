package com.lijukay.famecrew.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.fragments.CFPage1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenFileActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private LottieAnimationView lottieAnimationView;
    private MaterialButton nextCreate, cancelChose;
    private MaterialToolbar materialToolbar;
    private SharedPreferences membersPreference, firstStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstStart = getSharedPreferences("firstStart", 0);
        membersPreference = getSharedPreferences("Members", 0);

        if (!firstStart.getBoolean("firstStart", true)){
            startActivity(new Intent(this, MainActivity.class));
        }

        setContentView(R.layout.activity_open_file);

        int color = SurfaceColors.SURFACE_2.getColor(this);
        materialToolbar = findViewById(R.id.materialToolbar);
        frameLayout = findViewById(R.id.fragmentHolder);
        RelativeLayout relativeLayout = findViewById(R.id.buttonLayout);
        nextCreate = findViewById(R.id.createNext);
        cancelChose = findViewById(R.id.cancelChoose);
        lottieAnimationView = findViewById(R.id.lottieAnimation);

        materialToolbar.setBackgroundColor(color);
        getWindow().setNavigationBarColor(color);
        getWindow().setStatusBarColor(color);
        relativeLayout.setBackgroundColor(color);

        frameLayout.setVisibility(View.GONE);
        lottieAnimationView.setVisibility(View.VISIBLE);

        nextCreate.setOnClickListener(v -> {
            lottieAnimationView.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, new CFPage1(nextCreate, cancelChose, materialToolbar)).commit();

        });

        cancelChose.setOnClickListener(v -> mGetContent.launch("application/json"));
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        try {
            InputStream inputStream = getContentResolver().openInputStream(result);
            File outputFile = createOutputFile();

            if (inputStream != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
                membersPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();

                firstStart.edit().putBoolean("firstStart", false).apply();

                startActivity(new Intent(OpenFileActivity.this, MainActivity.class));
            } else {
                new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Unable to read file")
                        .setMessage("There was an error while reading the file. Try again")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    private File createOutputFile() {
        String destination = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + getString(R.string.app_name) + "-Members.json";
        return new File(destination);
    }
}