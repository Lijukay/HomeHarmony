package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class ExercisesFragment extends Fragment {

    ExtendedFloatingActionButton efab;
    RecyclerView recyclerView;
    ExerciseAdapter exerciseAdapter;
    ArrayList<Exercise> exercises;
    ArrayList<Member> members;
    SharedPreferences exercisesPreference, membersPreference;

    public ExercisesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exercises, container, false);

        exercisesPreference = requireContext().getSharedPreferences("Exercises", 0);
        membersPreference = requireContext().getSharedPreferences("Members", 0);

        efab = v.findViewById(R.id.addExercise);
        recyclerView = v.findViewById(R.id.exercisesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        exercises = new ArrayList<>();

        if (exercisesPreference.getString("filePath", null) != null) {
            getFileContent(new File(exercisesPreference.getString("filePath", null)));
        } else if (exercisesPreference.getBoolean("firstStart", true)) {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.first_start_dialog_title_exercises))
                    .setMessage(getString(R.string.first_start_dialog_message_exercises))
                    .setPositiveButton(getString(R.string.okay), (dialog, which) -> {
                        dialog.cancel();
                    })
                    .setOnCancelListener(dialog -> exercisesPreference.edit().putBoolean("firstStart", false).apply())
                    .show();
        } else if (exercisesPreference.getBoolean("shouldExist", false)) {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.file_not_found_title))
                    .setMessage(getString(R.string.file_not_found_exercise))
                    .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                    .show();
        }

        exerciseAdapter = new ExerciseAdapter(requireContext(), exercises, null);
        recyclerView.setAdapter(exerciseAdapter);



            efab.setOnClickListener(v1 ->
                    new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.exercises_dialog_title))
                    .setMessage(getString(R.string.exercises_dialog_message))
                    .setPositiveButton(getString(R.string.add_exercise), (dialog, which) -> addNewExercise())
                    .setNeutralButton(getString(R.string.from_file), (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show());

            return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewExercise() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);

        builder.setTitle(getString(R.string.new_exercise_title));

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_exercise_dialog, (ViewGroup) getView(), false);
        TextInputLayout exerciseName = viewInflated.findViewById(R.id.exerciseNameTF);
        TextInputLayout member = viewInflated.findViewById(R.id.memberTF);
        builder.setView(viewInflated);

        builder.setPositiveButton(getString(R.string.add), (dialog, which) -> {
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

        if (membersPreference.getString("filePath", null) != null){
            File file = new File(membersPreference.getString("filePath", null));
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


        exercises = gson.fromJson(jsonString, exerciseType);
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
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhe";
            File file = new File(destination);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            exercisesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && getFileExtension(result.toString()).equals("hhe")) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(result);
                File outputFile = createOutputFile();

                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    exercisesPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();
                    File file = new File(outputFile.getAbsolutePath());
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
                    Type exercisesType = new TypeToken<ArrayList<Exercise>>(){}.getType();
                    exercises = gson.fromJson(jsonString, exercisesType);
                    exerciseAdapter.updateData(exercises);
                    addExercisesToFile();
                } else {
                    new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                            .setTitle(getString(R.string.read_file_dialog_error_title))
                            .setMessage(getString(R.string.read_file_dialog_error_message))
                            .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                            .show();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (result != null) {
            if (getFileExtension(result.toString()).equals("hhr") || getFileExtension(result.toString()).equals("hhm")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle(getString(R.string.read_file_extension_not_valid_title))
                        .setMessage(getString(R.string.read_file_extension_not_valid_message_exercises))
                        .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                        .show();
            } else if (!result.toString().endsWith("hhr") && !result.toString().endsWith("hhe") && !result.toString().endsWith("hhm")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle(getString(R.string.read_file_extension_not_supported_title))
                        .setMessage(getString(R.string.read_file_extension_not_supported_message_exercises))
                        .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                        .show();
            }
        }
    });
    private String getFileExtension(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex < filePath.length() - 1) {
                return filePath.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }
    private File createOutputFile() {
        String destination = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name) + ".hhe";
        return new File(destination);
    }
}