package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.adapter.ExerciseAdapter;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class ExercisesFragment extends Fragment {

    ExtendedFloatingActionButton efab;
    String type;
    MaterialToolbar title;
    RecyclerView recyclerView;
    ExerciseAdapter exerciseAdapter;
    ArrayList<Exercise> exercises;
    ArrayList<Member> members;

    public ExercisesFragment(String type, MaterialToolbar title) {
        this.type = type;
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exercises, container, false);

        efab = v.findViewById(R.id.addExercise);
        recyclerView = v.findViewById(R.id.exercisesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        exercises = new ArrayList<>();

        if (requireContext().getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null) != null) {
            getFileContent(new File(requireContext().getSharedPreferences("file_path_ex", 0).getString("file_path_ex", null)));
        }

        exerciseAdapter = new ExerciseAdapter(requireContext(), exercises, null);
        recyclerView.setAdapter(exerciseAdapter);

        if (type.equals("unsorted")){
            title.setTitle("Unsorted Exercises");
            efab.setOnClickListener(v12 -> addNewUnsortedExercise());
        } else if (type.equals("all")){
            title.setTitle("All exercises");
            efab.setOnClickListener(v1 -> addNewExercise());
        }

        // TODO: 30.05.2023 Show exercises depended on what exercises have to be shown (unsorted or every)

        return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewUnsortedExercise() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);

        builder.setTitle("New Exercise");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_exercise_dialog, (ViewGroup) getView(), false);
        TextInputLayout exerciseName = viewInflated.findViewById(R.id.exerciseNameTF);
        TextInputLayout member = viewInflated.findViewById(R.id.memberTF);
        builder.setView(viewInflated);
        member.setEnabled(false);
        member.setVisibility(View.GONE);

        builder.setPositiveButton("Add", (dialog, which) -> {
            if (!Objects.requireNonNull(exerciseName.getEditText()).getText().toString().trim().equals("")) {
                exercises.add(new Exercise(exerciseName.getEditText().getText().toString().trim(), new Member("", "")));
                exerciseAdapter.notifyDataSetChanged();
                addExercisesToFile();
            }
        });
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewExercise() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);

        builder.setTitle("New Exercise");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_exercise_dialog, (ViewGroup) getView(), false);
        TextInputLayout exerciseName = viewInflated.findViewById(R.id.exerciseNameTF);
        TextInputLayout member = viewInflated.findViewById(R.id.memberTF);
        builder.setView(viewInflated);

        builder.setPositiveButton("Add", (dialog, which) -> {
            if (!Objects.requireNonNull(exerciseName.getEditText()).getText().toString().trim().equals("")) {
                if (Objects.requireNonNull(member.getEditText()).getText().toString().trim().equals("")) {
                    exercises.add(new Exercise(exerciseName.getEditText().getText().toString().trim(), new Member("", "")));
                } else {
                    exercises.add(new Exercise(exerciseName.getEditText().getText().toString().trim(), getMember(member.getEditText().getText().toString().trim())));
                }
                exerciseAdapter.notifyDataSetChanged();
                addExercisesToFile();
            }
        });
        builder.show();
    }

    private Member getMember(String memberNickname) {

        if (requireContext().getSharedPreferences("file_path_members", 0).getString("file_path_members", null) != null){
            File file = new File(requireContext().getSharedPreferences("file_path_members", 0).getString("file_path_members", null));
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

            members = new ArrayList<>();
            Gson gson = new Gson();
            String jsonString = fileContent.toString();

            Type memberType = new TypeToken<ArrayList<Member>>(){}.getType();
            members = gson.fromJson(jsonString, memberType);

            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).getNickname().equals(memberNickname)){
                    return members.get(i);
                }
            }
        }
        return new Member("", "");
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

        if (type.equals("all")){
            exercises = gson.fromJson(jsonString, exerciseType);
        } else {
            ArrayList<Exercise> exercises1 = gson.fromJson(jsonString, exerciseType);
            for(int i = 0; i < exercises1.size(); i++) {
                if (exercises1.get(i).getMember().getPrename().equals("") && exercises1.get(i).getMember().getPrename().equals("")) {
                    exercises.add(exercises1.get(i));
                }
            }
        }
    }

    private void addExercisesToFile() {
        Gson gson = new Gson();
        if (exercises.size() != 0) {
            String jsonString = gson.toJson(exercises);
            saveJsonAsFile(requireContext(), jsonString);
        }
    }

    private void saveJsonAsFile(Context context, String jsonString) {
        try {
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + getString(R.string.app_name) + "-E.famecrew";
            File file = new File(destination);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            Toast.makeText(context, "File was saved successfully", Toast.LENGTH_SHORT).show();
            context.getSharedPreferences("file_path_ex", 0).edit().putString("file_path_ex", file.getAbsolutePath()).apply();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}