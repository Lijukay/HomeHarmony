package com.lijukay.famecrew.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
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

        SharedPreferences exercisePreference = requireContext().getSharedPreferences("Exercises", 0);
        String filePath = exercisePreference.getString("filePath", null);
        SharedPreferences settingsPreference = requireContext().getSharedPreferences("Settings", 0);
        String user = settingsPreference.getString("User", "user");

        LinearProgressIndicator progress = v.findViewById(R.id.exerciseProgress);

        TextView progressTextView = v.findViewById(R.id.progressText);
        TextView dailyProgressTextView = v.findViewById(R.id.dailyProgressMessage);


        if(!user.equals("user")){
            if (filePath != null) {
                dailyProgressTextView.setVisibility(View.GONE);
                getFileContent(new File(filePath));
                if (exercises.size() != 0) {
                    progress.setVisibility(View.VISIBLE);
                    progressTextView.setVisibility(View.VISIBLE);
                    progress.setMax(exercises.size()); //TODO: GET EXERCISES COUNT (FOR SPECIFIC USER) AND SET MAX TO IT
                    progress.setProgress(1); // TODO: 04.06.2023 GET HOW MANY EXERCISES ARE DONE AND SET PROGRESS TO IT
                    progressTextView.setText(progress.getProgress() + "/" + progress.getMax());
                } else {
                    progressTextView.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    dailyProgressTextView.setVisibility(View.VISIBLE);
                    dailyProgressTextView.setText(getString(R.string.home_no_exercises));
                }
            } else {
                progressTextView.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                dailyProgressTextView.setVisibility(View.VISIBLE);
                dailyProgressTextView.setText(getString(R.string.home_no_exercises));
            }
        }else {
            progressTextView.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            dailyProgressTextView.setVisibility(View.VISIBLE);
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