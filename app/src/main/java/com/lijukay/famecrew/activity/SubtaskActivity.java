package com.lijukay.famecrew.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.BuildConfig;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.adapter.SubtasksAdapter;
import com.lijukay.famecrew.adapter.SubtasksAdapterSimpleItem;
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;
import com.lijukay.famecrew.objects.Subtask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SubtaskActivity extends AppCompatActivity implements OnLongClickInterface {

    private String taskFilePath;
    private ArrayList<Exercise> exercises;
    private ArrayList<Subtask> subtasks;
    private SubtasksAdapter adapter;
    private int index = -1;
    private Exercise ex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        int surfaceColorLvl2 = SurfaceColors.SURFACE_2.getColor(this);

        taskFilePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + ".hhe";
        Intent intent = getIntent();
        subtasks = new ArrayList<>();
        MaterialToolbar toolbar = findViewById(R.id.titleBar);
        AppBarLayout topAppBar = findViewById(R.id.top_app_bar);
        RecyclerView subtasksView = findViewById(R.id.subtasksRV);

        topAppBar.setBackgroundColor(surfaceColorLvl2);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (intent.getExtras() != null) {
            String exName = intent.getStringExtra("Exercise name");
            ex = null;

            toolbar.setTitle(exName);

            try {
                getFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < exercises.size(); i++) {
                if (exercises.get(i).getExName().equals(exName)) {
                    ex = exercises.get(i);
                    index = i;
                }
            }

            if (ex != null) {
                subtasks = ex.getSubtasks();
            }

            subtasksView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            adapter = new SubtasksAdapter(this, subtasks, this);
            subtasksView.setAdapter(adapter);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {

            String[] extensions = new String[]{
                    ".hhe",
                    ".hhm",
                    ".hhr"
            };
            boolean[] selected = new boolean[] {
                    false,
                    false,
                    false
            };

            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Share files")
                    .setMultiChoiceItems(new String[]{"Exercises", "Members", "Rules"}, selected, (dialog, which, isChecked) -> selected[which] = isChecked)
                    .setPositiveButton("Share", (dialog, which) -> sendFiles(extensions, selected))
                    .show();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(SubtaskActivity.this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.addItem) {
            addNewSubtask();
        }
        return false;
    }

    private void addNewSubtask() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View v = View.inflate(this, R.layout.dialog_add_subtask, null);

        TextInputLayout subtask = v.findViewById(R.id.subtaskTitle);
        TextInputLayout subtaskInfo = v.findViewById(R.id.subtaskMessage);
        MaterialButton addButton = v.findViewById(R.id.buttonAdd);

        addButton.setOnClickListener(v1 -> {
            String subtaskTitle = Objects.requireNonNull(subtask.getEditText()).getText().toString().trim();
            String subtaskInfoS = TextUtils.isEmpty(Objects.requireNonNull(subtaskInfo.getEditText()).getText().toString().trim()) ? null : Objects.requireNonNull(subtaskInfo.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(subtaskTitle)) {
                Toast.makeText(SubtaskActivity.this, "Subtask needs a title", Toast.LENGTH_SHORT).show();
                return;
            }

            Subtask subtask1 = new Subtask(subtaskTitle, subtaskInfoS);
            subtasks.add(subtask1);
            updateSubtasks();
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

    private void saveFileContent() throws IOException {

        if (index != -1 && ex != null) {
            exercises.set(index, new Exercise(ex.getExName(), ex.getMember(), ex.isDone(), ex.getDoneDay(), ex.getDoneMonth(), ex.getDoneYear(), ex.getDoneByMember(), ex.isVoluntary(), subtasks));
            Gson gson = new Gson();
            String jsonString = gson.toJson(exercises);

            FileOutputStream fos = new FileOutputStream(taskFilePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(jsonString);
            bw.close();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateSubtasks() {
        adapter.notifyDataSetChanged();
    }

    public void sendFiles(String[] extensions, boolean[] selected) {
        ArrayList<Uri> files = new ArrayList<>();

        for (int i = 0; i < extensions.length; i++) {

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + extensions[i]);

            if (selected[i] && file.exists()) {
                Uri fileUri = FileProvider.getUriForFile(SubtaskActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                files.add(fileUri);
            }
        }

        if (files.size() != 0) {
            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("application/octet-stream");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            for (int i = 0; i < files.size(); i++) {
                for (ResolveInfo ri : getPackageManager().queryIntentActivities(share, PackageManager.MATCH_DEFAULT_ONLY)){
                    grantUriPermission(ri.activityInfo.packageName, files.get(i), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            startActivity(Intent.createChooser(share, "Share files"));
        }
    }

    @Override
    public void onLongClick(int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View v = View.inflate(this, R.layout.dialog_update_subtask, null);

        TextInputLayout subtask = v.findViewById(R.id.subtaskTitle);
        TextInputLayout subtaskInfo = v.findViewById(R.id.subtaskMessage);
        MaterialButton updateButton = v.findViewById(R.id.buttonUpdate);
        MaterialButton deleteButton = v.findViewById(R.id.buttonDelete);

        String stTitle = subtasks.get(position).getSubTaskName();
        String stInfo = subtasks.get(position).getSubTaskInfo();

        if (stTitle != null) {
            Objects.requireNonNull(subtask.getEditText()).setText(stTitle);
        }

        if (stInfo != null) {
            Objects.requireNonNull(subtaskInfo.getEditText()).setText(stInfo);
        }
        
        updateButton.setOnClickListener(v1 -> {
            String subtaskTitle = Objects.requireNonNull(subtask.getEditText()).getText().toString().trim();
            String subtaskInfoS = TextUtils.isEmpty(Objects.requireNonNull(subtaskInfo.getEditText()).getText().toString().trim()) ? null : Objects.requireNonNull(subtaskInfo.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(subtaskTitle)) {
                Toast.makeText(SubtaskActivity.this, "Subtask needs a title", Toast.LENGTH_SHORT).show();
                return;
            }

            Subtask subtask1 = new Subtask(subtaskTitle, subtaskInfoS);
            subtasks.set(position, subtask1);
            updateSubtasks();
            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dialog.dismiss();
        });
        
        deleteButton.setOnClickListener(v12 -> {
            removeSubtask(position);
            dialog.dismiss();
        });

        dialog.setContentView(v);
        dialog.show();
    }

    public void removeSubtask (int position) {
        subtasks.remove(position);
        updateSubtasks();
        try {
            saveFileContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}