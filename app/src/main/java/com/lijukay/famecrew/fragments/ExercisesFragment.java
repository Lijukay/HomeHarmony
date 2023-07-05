package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.activity.SubtaskActivity;
import com.lijukay.famecrew.activity.MainActivity;
import com.lijukay.famecrew.adapter.ExerciseAdapter;
import com.lijukay.famecrew.adapter.SubtasksAdapterSimpleItem;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;
import com.lijukay.famecrew.objects.Subtask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ExercisesFragment extends Fragment implements OnClickInterface, OnLongClickInterface {

    private ExerciseAdapter exerciseAdapter;
    private ArrayList<Exercise> exercises;
    private String DESTINATION_FILE;
    private String DESTINATION_MEMBERS_FILE;
    private int dayOfMonth, month, year;

    public ExercisesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DESTINATION_FILE = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + ".hhe";
        DESTINATION_MEMBERS_FILE = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + ".hhm";
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_exercises, container, false);

        Calendar calendar = Calendar.getInstance();
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        RecyclerView recyclerView = v.findViewById(R.id.exercisesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        exercises = new ArrayList<>();

        if (new File(DESTINATION_FILE).exists()){
            try {
                getFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        exerciseAdapter = new ExerciseAdapter(requireContext(), exercises, this, this);
        updateExercises();

        recyclerView.setAdapter(exerciseAdapter);

        try {
            updateFinishedExercisesIfNeeded();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            updateMembersExerciseIfNeeded();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return v;
    }

    private void updateMembersExerciseIfNeeded() throws IOException {
        for (Exercise exercise : exercises) {
            Member member = exercise.getMember();
            if (member != null && getMember(member.getNickname()) == null) {
                exercises.set(exercises.indexOf(exercise), new Exercise(exercise.getExName(), null, exercise.isDone(), exercise.getDoneDay(), exercise.getDoneMonth(), exercise.getDoneYear(), exercise.getDoneByMember(), exercise.isVoluntary(), exercise.getSubtasks()));
            }
        }

        saveFileContent();
    }

    private void updateFinishedExercisesIfNeeded() throws IOException {
        for (Exercise exercise : exercises) {
            if (exercise.isDone() && (dayOfMonth > exercise.getDoneDay() || month > exercise.getDoneMonth() || year > exercise.getDoneYear())) {
                exercises.set(exercises.indexOf(exercise), new Exercise(exercise.getExName(), exercise.getMember(), false, 0, 0, 0, null, exercise.isVoluntary(), exercise.getSubtasks()));
            }
        }
        saveFileContent();
    }

    private void addNewExercise() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        ArrayList<Subtask> subtasks = new ArrayList<>();
        SubtasksAdapterSimpleItem subtasksAdapter = new SubtasksAdapterSimpleItem(requireContext(), subtasks);

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, (ViewGroup) getView(), false);

        TextInputLayout exerciseName = v.findViewById(R.id.exerciseNameTF);
        TextInputLayout member = v.findViewById(R.id.memberTF);
        MaterialSwitch voluntary = v.findViewById(R.id.voluntaryCheck);
        MaterialButton addToSubtask = v.findViewById(R.id.addToSubtasks);
        TextInputEditText editText = v.findViewById(R.id.subtasks);
        RecyclerView recyclerView = v.findViewById(R.id.subtasksList);
        MaterialCardView listHolderCard = v.findViewById(R.id.list_holder_card);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerView.setAdapter(subtasksAdapter);

        voluntary.setOnCheckedChangeListener((buttonView, isChecked) -> member.setVisibility(isChecked ? View.GONE : View.VISIBLE));

        member.setVisibility(voluntary.isChecked() ? View.GONE : View.VISIBLE);

        addToSubtask.setOnClickListener(v12 -> {
            listHolderCard.setVisibility(View.VISIBLE);
            String subtaskName = Objects.requireNonNull(editText.getText()).toString().trim();

            if (!TextUtils.isEmpty(subtaskName)) {
                subtasks.add(new Subtask(subtaskName, null));
                subtasksAdapter.updateData(subtasks);
                editText.setText("");
            } else {
                Toast.makeText(requireContext(), getString(R.string.nothing_to_add), Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.buttonAdd).setOnClickListener(v1 -> {
            String name = Objects.requireNonNull(exerciseName.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), getString(R.string.name_required), Toast.LENGTH_SHORT).show();
                return;
            }
            for (Exercise exercise : exercises) {
                if (exercise.getExName().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
                    Toast.makeText(requireContext(), getString(R.string.same_task_exists), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Member exerciseMember = null;
            if (!voluntary.isChecked()) {
                try {
                    exerciseMember = getMember(Objects.requireNonNull(member.getEditText()).getText().toString().trim());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (exerciseMember == null) {
                    voluntary.setChecked(true);
                }
            }

            Exercise exercise = new Exercise(name, exerciseMember, false, 0, 0, 0, null, voluntary.isChecked(), subtasks);
            exercises.add(exercise);
            updateExercises();
            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dialog.dismiss();
        });

        dialog.setContentView(v);
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateExercises() {
        exerciseAdapter.notifyDataSetChanged(); // TODO: 19.06.2023 Check if used elsewhere, else replace with notifyDataInserted();
    }

    public void removeExercise(int position) {
        exercises.remove(position);
        updateExercises();
        try {
            saveFileContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getFileContent() throws IOException {
        StringBuilder fileContentBuilder = new StringBuilder();
        FileInputStream fis = new FileInputStream(DESTINATION_FILE);
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

    private void saveFileContent() throws IOException {
        Gson gson = new Gson();
        String exerciseJson = gson.toJson(exercises);

        FileOutputStream fos = new FileOutputStream(DESTINATION_FILE);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(exerciseJson);
        bw.close();
    }

    private Member getMember(String memberNickname) throws IOException {
        StringBuilder fileContentBuilder = new StringBuilder();

        if (new File(DESTINATION_MEMBERS_FILE).exists()){
            FileInputStream fis = new FileInputStream(DESTINATION_MEMBERS_FILE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                fileContentBuilder.append(line).append("\n");
            }

            br.close();

            Gson gson = new Gson();
            ArrayList<Member> members = gson.fromJson(fileContentBuilder.toString(), new TypeToken<ArrayList<Member>>() {}.getType());

            for (Member member : members) {
                if (member.getNickname().equals(memberNickname)) {
                    return member;
                }
            }
            return null;
        }

        return null;
    }

    public ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && ((MainActivity) requireActivity()).getFileExtension(result.toString()).equals("hhe")) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(result);

                File outputFile = new File(DESTINATION_FILE);

                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
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
                    saveFileContent();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem addItem = menu.findItem(R.id.addItem);
        addItem.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addItem) {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.tasks))
                    .setMessage(getString(R.string.create_task_dialog_message))
                    .setPositiveButton(getString(R.string.new_task), (dialog, which) -> addNewExercise())
                    .setNeutralButton(getString(R.string.from_file), (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(int position, String type) {
        if (type.equals("img")) {
            Exercise exercise = exercises.get(position);
            Member doneByMember = exercise.getDoneByMember();
            if (doneByMember != null) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle(getString(R.string.task_done))
                        .setMessage(getString(R.string.task_done_message, doneByMember.getPrename(), doneByMember.getNickname()))
                        .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                        .show();
            }
        } else {
            Intent intent = new Intent(requireContext(), SubtaskActivity.class);
            intent.putExtra("Exercise name", exercises.get(position).getExName());
            startActivity(intent);
        }
    }

    @Override
    public void onLongClick(int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_task, (ViewGroup) getView(), false);
        TextInputLayout exerciseName = viewInflated.findViewById(R.id.exercisesName);
        MaterialSwitch voluntary = viewInflated.findViewById(R.id.voluntaryCheck);
        TextInputLayout membersNickname = viewInflated.findViewById(R.id.exerciseMembersNickname);
        MaterialSwitch doneSwitch = viewInflated.findViewById(R.id.doneSwitch);
        TextView doneInfo = viewInflated.findViewById(R.id.doneInfo);
        TextInputLayout doneMember = viewInflated.findViewById(R.id.doneMembersNickname);

        doneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                doneInfo.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                doneMember.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });

        voluntary.setOnCheckedChangeListener((buttonView, isChecked) -> membersNickname.setVisibility(isChecked ? View.GONE : View.VISIBLE));

        Exercise exercise = exercises.get(position);
        Objects.requireNonNull(exerciseName.getEditText()).setText(exercise.getExName());

        Member member = exercise.getMember();

        if (member != null) {
            Objects.requireNonNull(membersNickname.getEditText()).setText(member.getNickname());
        }

        Member doneByMember = exercise.getDoneByMember();
        if (doneByMember != null) {
            Objects.requireNonNull(doneMember.getEditText()).setText(doneByMember.getNickname());
        }

        doneSwitch.setChecked(exercise.isDone());
        voluntary.setChecked(exercise.isVoluntary());
        doneInfo.setVisibility(exercise.isDone() ? View.VISIBLE : View.GONE);
        doneMember.setVisibility(exercise.isDone() ? View.VISIBLE : View.GONE);
        membersNickname.setVisibility(exercise.isVoluntary() ? View.GONE : View.VISIBLE);

        viewInflated.findViewById(R.id.buttonUpdate).setOnClickListener(v -> {

            String newName = Objects.requireNonNull(exerciseName.getEditText()).getText().toString().trim();
            String newMemberNickname = Objects.requireNonNull(membersNickname.getEditText()).getText().toString().trim();
            boolean newIsDone = doneSwitch.isChecked();
            String newDoneByNickname = Objects.requireNonNull(doneMember.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(requireContext(), getString(R.string.name_required), Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < exercises.size(); i++) {
                if (exercises.get(i).getExName().toLowerCase(Locale.ROOT).equals(newName.toLowerCase(Locale.ROOT)) && i != position) {
                    Toast.makeText(requireContext(), getString(R.string.same_task_exists), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Member newMember;
            Member newDoneByMember;

            try {
                newMember = getMember(newMemberNickname);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                newDoneByMember = newIsDone ? getMember(newDoneByNickname) : null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (voluntary.isChecked()) {
                if (newIsDone && newDoneByMember == null) {
                    newIsDone = false;
                    Toast.makeText(requireContext(), getString(R.string.member_does_not_exist), Toast.LENGTH_SHORT).show();
                }
                exercises.set(position, new Exercise(newName, null, newIsDone, dayOfMonth, month, year, newDoneByMember, true, exercise.getSubtasks()));
            } else {
                if (newIsDone && newDoneByMember == null) {
                    newIsDone = false;
                    Toast.makeText(requireContext(), getString(R.string.member_does_not_exist), Toast.LENGTH_SHORT).show();
                }
                if (newMember == null) {
                    voluntary.setChecked(true);
                    Toast.makeText(requireContext(), getString(R.string.member_does_not_exist), Toast.LENGTH_SHORT).show();
                }
                exercises.set(position, new Exercise(newName, newMember, newIsDone, dayOfMonth, month, year, newDoneByMember, voluntary.isChecked(), exercise.getSubtasks()));
            }

            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            exerciseAdapter.updateData(exercises);
            dialog.dismiss();
        });

        viewInflated.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            removeExercise(position);
            dialog.dismiss();
        });

        dialog.setContentView(viewInflated);
        dialog.show();
    }
}