package com.lijukay.famecrew.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.objects.Exercise;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ArrayList<Exercise> exercises;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        String destinationExercise = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name) + ".hhe";
        SharedPreferences settingsPreference = requireContext().getSharedPreferences("Settings", 0);
        String user = settingsPreference.getString("User", "user");

        LinearProgressIndicator progress = v.findViewById(R.id.exerciseProgress);

        TextView dailyProgressTextView = v.findViewById(R.id.dailyProgressMessage);

        if(!user.equals("user")){
            if (new File(destinationExercise).exists() && exercises.size() != 0) {
                dailyProgressTextView.setVisibility(View.GONE);
                getFileContent(new File(destinationExercise));

                progress.setVisibility(View.VISIBLE);
                dailyProgressTextView.setVisibility(View.VISIBLE);
                progress.setMax(exercises.size());
                progress.setProgress(1);
                dailyProgressTextView.setText(getString(R.string.count_of_done_tasks, progress.getProgress(), progress.getMax()));
            } else {
                progress.setVisibility(View.GONE);
                dailyProgressTextView.setVisibility(View.VISIBLE);
                dailyProgressTextView.setText(getString(R.string.cannot_use_feature_tasks));
            }
        }else {
            progress.setVisibility(View.GONE);
            dailyProgressTextView.setVisibility(View.VISIBLE);
            dailyProgressTextView.setText(getString(R.string.cannot_use_feature_user));
        }

        return v;
    }

    private void getFileContent(File file) {
        StringBuilder fileContent = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        exercises = new ArrayList<>();
        Gson gson = new Gson();
        String jsonString = fileContent.toString();
        Type exerciseType = new TypeToken<ArrayList<Exercise>>(){}.getType();

        exercises = gson.fromJson(jsonString, exerciseType);
    }

}