package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
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
    String type;
    MaterialToolbar title;
    RecyclerView recyclerView;
    ExerciseAdapter exerciseAdapter;
    ArrayList<Exercise> exercises;
    ArrayList<Member> members;
    SharedPreferences exercisesPreference, membersPreference;

    public ExercisesFragment(String type, MaterialToolbar title) {
        this.type = type;
        this.title = title;
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
        }

        exerciseAdapter = new ExerciseAdapter(requireContext(), exercises, null);
        recyclerView.setAdapter(exerciseAdapter);

        if (type.equals("unsorted")){
            title.setTitle("Unsorted Exercises");
            efab.setOnClickListener(v12 ->
                    new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Exercises")
                    .setMessage("You can create a new file yourself by clicking on \"Add exercise\" and open a file, that includes exercises, by clicking on \"From File\"")
                    .setPositiveButton("Add exercise", (dialog, which) -> addNewUnsortedExercise())
                    .setNeutralButton("From file", (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show());

        } else if (type.equals("all")){
            title.setTitle("All exercises");
            efab.setOnClickListener(v1 ->
                    new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Exercises")
                    .setMessage("You can create a new file yourself by clicking on \"Add exercise\" and open a file, that includes exercises, by clicking on \"From File\"")
                    .setPositiveButton("Add exercise", (dialog, which) -> addNewExercise())
                    .setNeutralButton("From file", (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show());
        }

        ViewCompat.setOnApplyWindowInsetsListener(efab, (v1, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) efab.getLayoutParams();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v1.getLayoutParams();
            mlp.bottomMargin = insets.bottom + lp.bottomMargin;
            v1.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

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
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + getString(R.string.app_name) + "-Exercises.famecrew";
            File file = new File(destination);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            Toast.makeText(context, "File was saved successfully", Toast.LENGTH_SHORT).show();
            exercisesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && result.toString().endsWith("-Exercises.famecrew")) {
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
        } else if (result != null) {
            if (!getFileExtension(result.toString()).equals("famecrew")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("File type not supported")
                        .setMessage("Please make sure, you are using a file, where the extension is famecrew. The application is not able to read other files than that.")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            } else if (!result.toString().endsWith("-Exercises.famecrew")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Almost")
                        .setMessage("Please make sure, your file ends with \"-Exercises.famecrew\".")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }
        } else {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Oh no!")
                    .setMessage("This was unexpected. Try again.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
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
        String destination = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + getString(R.string.app_name) + "-Exercises.famecrew";
        return new File(destination);
    }

}