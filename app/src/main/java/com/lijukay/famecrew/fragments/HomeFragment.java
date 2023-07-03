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
            dailyProgressTextView.setVisibility(View.GONE);
            getFileContent(new File(destinationExercise));
            if (exercises.size() != 0) {
                progress.setVisibility(View.VISIBLE);
                dailyProgressTextView.setVisibility(View.VISIBLE);
                progress.setMax(exercises.size()); //TODO: GET EXERCISES COUNT (FOR SPECIFIC USER) AND SET MAX TO IT
                progress.setProgress(1); // TODO: 04.06.2023 GET HOW MANY EXERCISES ARE DONE AND SET PROGRESS TO IT
                //You have finished %1$d of %2$d of your exercises
                //dailyProgressTextView.setText(getString(R.string.progress, progress.getProgress(), progress.getMax()));
                dailyProgressTextView.setText("You have finished " + progress.getProgress() + " of " + progress.getMax() + "of your exercises.");

            } else {
                progress.setVisibility(View.GONE);
                dailyProgressTextView.setVisibility(View.VISIBLE);
                dailyProgressTextView.setText("This can't be used as it seems like there are no tasks.");
            }
        }else {
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