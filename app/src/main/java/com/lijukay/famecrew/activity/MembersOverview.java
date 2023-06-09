package com.lijukay.famecrew.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.UncaughtExceptionHandler;
import com.lijukay.famecrew.adapter.ExerciseAdapter;
import com.lijukay.famecrew.objects.Exercise;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MembersOverview extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ArrayList<Exercise> exercises;
    private String exercisesFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_overview);

        SharedPreferences exercisesPreference = getSharedPreferences("Exercises", 0);
        exercisesFilePath = exercisesPreference.getString("filePath", null);

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

        toolbar = findViewById(R.id.materialToolbar);
        RecyclerView membersExercisesRV = findViewById(R.id.membersExercises);

        if (getIntent().getExtras() != null) {
            ExerciseAdapter adapter = null;
            String prename = getIntent().getStringExtra("MembersPreName");
            String nickname = getIntent().getStringExtra("MembersNickName");

            toolbar.setTitle(prename + " (" + nickname + ")");

            exercises = new ArrayList<>();

            try {
                getMembersExercises();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (exercises != null && exercises.size() != 0) {
                adapter = new ExerciseAdapter(MembersOverview.this, exercises, null);
            } else if (exercises != null) {
                // TODO: 10.06.2023 Show MaterialAlterDialog that tells the user that no exercises were found and close the activity after that (onBackPressed
            }
            membersExercisesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            if (adapter != null) {
                membersExercisesRV.setAdapter(adapter);
            }
        }
    }

    private void getMembersExercises() throws IOException {
        if (exercisesFilePath != null) {
            FileInputStream inputStream = new FileInputStream(exercisesFilePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder jsonString = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line);
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Exercise>>(){}.getType();

            ArrayList<Exercise> exercisesPre = gson.fromJson(jsonString.toString(), type);

            for (int i = 0; i < exercisesPre.size(); i++) {
                if ((exercisesPre.get(i).getMember().getPrename() + " (" + exercisesPre.get(i).getMember().getNickname() + ")").contentEquals(toolbar.getTitle())) {
                    exercises.add(exercisesPre.get(i));
                }
            }
        } else {
            // TODO: 10.06.2023 Show MaterialAlertDialog that tells the user that no exercises were found and close this activity after
        }
    }
}