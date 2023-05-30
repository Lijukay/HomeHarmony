package com.lijukay.famecrew.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.adapter.ExerciseAdapter;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MemberOverview extends Fragment implements OnClickInterface {

    private final String name;
    private final MaterialToolbar materialToolbar;
    private ArrayList<Exercise> exercises;

    public MemberOverview(String name, MaterialToolbar materialToolbar) {
        this.name = name;
        this.materialToolbar = materialToolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_member_overview, container, false);

        materialToolbar.setTitle(name);
        RecyclerView rv = v.findViewById(R.id.memberExercises);
        rv.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        if (requireContext().getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null) != null) {
            getFileContent(new File(requireContext().getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null)));

            if (exercises.size() == 0) {
                exercises.add(new Exercise(name, new Member("No Exercise", "")));
            }

            ExerciseAdapter exerciseAdapter = new ExerciseAdapter(requireContext(), exercises, this);
            rv.setAdapter(exerciseAdapter);
        } else {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("No file")
                    .setMessage("The file, that contains exercises has not been found. Please go to Exercises overview and create a new file or click on choose file")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
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

        getExercise(fileContent);
    }

    private void getExercise(StringBuilder fileContent) {
        exercises = new ArrayList<>();
        String jsonString = fileContent.toString();
        Gson gson = new Gson();

        Type exerciseType = new TypeToken<ArrayList<Exercise>>(){}.getType();

        ArrayList<Exercise> exercises1 = gson.fromJson(jsonString, exerciseType);

        for (int i = 0; i < exercises1.size(); i++) {
            if ((exercises1.get(i).getMember().getPrename() + " (" + exercises1.get(i).getMember().getNickname() + ")").equals(name)) {
                exercises.add(exercises1.get(i));
            }
        }
    }

    @Override
    public void onItemClick(int position) {

    }
}