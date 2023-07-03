package com.lijukay.famecrew.activity;

import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.elevation.SurfaceColors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.adapter.ExerciseAdapter;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class MembersActivity extends AppCompatActivity {

    private ArrayList<Exercise> exercises;
    private String taskFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        int color = SurfaceColors.SURFACE_2.getColor(this);

        taskFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + ".hhe";
        AppBarLayout topAppBar = findViewById(R.id.titleBar);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar);
        RecyclerView membersExercisesRV = findViewById(R.id.membersExercises);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        topAppBar.setBackgroundColor(color);

        if (getIntent().getExtras() != null) {
            String prename = getIntent().getStringExtra("MembersPreName");
            String nickname = getIntent().getStringExtra("MembersNickName");

            toolbar.setTitle(prename + " (" + nickname + ")");
            if(new File(taskFilePath).exists()) {
                membersExercisesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                try {
                    getFileContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Iterator<Exercise> iterator = exercises.iterator();

                while (iterator.hasNext()) {
                    Exercise exercise = iterator.next();
                    Member member = exercise.getMember();

                    if (member == null || !member.getNickname().equals(nickname)) {
                        iterator.remove();
                    }
                }

                ExerciseAdapter adapter = new ExerciseAdapter(this, exercises, null, null);
                membersExercisesRV.setAdapter(adapter);
            }
        }
    }

    private void getFileContent() throws IOException {
        StringBuilder fileContentBuilder = new StringBuilder();
        FileInputStream fis = new FileInputStream(taskFilePath);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while ((line = br.readLine()) != null) {
            fileContentBuilder.append(line).append("\n");
        }

        br.close();

        Gson gson = new Gson();
        exercises = gson.fromJson(fileContentBuilder.toString(), new TypeToken<ArrayList<Exercise>>() {}.getType());
    }

}