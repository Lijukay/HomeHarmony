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
import com.lijukay.famecrew.adapter.RulesAdapter;
import com.lijukay.famecrew.objects.Rule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;


public class RulesOverview extends Fragment {

    private final MaterialToolbar title;
    private ArrayList<Rule> rules;
    private RulesAdapter rulesAdapter;
    private SharedPreferences rulesPreference;
    private Context context;
    boolean firstStartFragment;
    boolean fileShouldExist;

    public RulesOverview(MaterialToolbar title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rules_overview, container, false);

        context = requireContext();

        rulesPreference = context.getSharedPreferences("Rules", 0);
        String filePathString = rulesPreference.getString("filePath", null);
        firstStartFragment = rulesPreference.getBoolean("firstStart", true);
        fileShouldExist = rulesPreference.getBoolean("shouldExist", false);

        title.setTitle(getString(R.string.rulebook));

        ExtendedFloatingActionButton efab = v.findViewById(R.id.addRule);

        RecyclerView rulesRV = v.findViewById(R.id.rulesRV);
        rulesRV.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));

        rules = new ArrayList<>();

        if (filePathString != null) {
            getFileContent(new File(filePathString));
        } else if (firstStartFragment){
            new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.first_start_dialog_title_rules))
                    .setMessage(getString(R.string.first_start_dialog_message_rules))
                    .setPositiveButton(getString(R.string.okay), (dialog, which) -> {
                        dialog.cancel();
                    })
                    .setOnCancelListener(dialog -> rulesPreference.edit().putBoolean("firstStart", false).apply())
                    .show();
        } else if (fileShouldExist){
            new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.file_not_found_title))
                    .setMessage(getString(R.string.file_not_found_rules))
                    .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                    .show();
        }

        rulesAdapter = new RulesAdapter(context, rules, null);

        rulesRV.setAdapter(rulesAdapter);

        efab.setOnClickListener(v1 ->
                new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                .setTitle(getString(R.string.choose_or_create))
                .setMessage(getString(R.string.choose_or_create_rules))
                .setPositiveButton(getString(R.string.new_rule), (dialog, which) -> addNewRule())
                .setNeutralButton(getString(R.string.open_from_file), (dialog, which) -> mGetContent.launch("application/octet-stream"))
                .show());

        return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewRule() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);

        builder.setTitle(getString(R.string.new_rule));

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_rule_dialog, (ViewGroup) getView(), false);

        TextInputLayout ruleTitle = viewInflated.findViewById(R.id.ruleTitleTF);
        TextInputLayout ruleMessage = viewInflated.findViewById(R.id.ruleMessageTF);

        builder.setView(viewInflated);

        builder.setPositiveButton(getString(R.string.add), (dialog, which) -> {
            if (!Objects.requireNonNull(ruleTitle.getEditText()).getText().toString().trim().equals("") && !Objects.requireNonNull(ruleMessage.getEditText()).getText().toString().trim().equals("")) {
                rules.add(new Rule(ruleTitle.getEditText().getText().toString().trim(), ruleMessage.getEditText().getText().toString().trim()));
                rulesAdapter.notifyDataSetChanged();
                rulesPreference.edit().putBoolean("shouldExist", true).apply();
                addRulesToFile();
            } else {
                Toast.makeText(context, getString(R.string.rule_must_include_title_and_message), Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
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

        Gson gson = new Gson();
        String jsonString = fileContent.toString();
        Type rulesType = new TypeToken<ArrayList<Rule>>(){}.getType();
        rules = gson.fromJson(jsonString, rulesType);
    }

    private void addRulesToFile() {
        Gson gson = new Gson();
        if (rules.size() != 0) {
            String jsonString = gson.toJson(rules);
            saveJsonAsFile(context, jsonString);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveJsonAsFile(Context context, String jsonString) {
        try {
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhr";
            File file = new File(destination);

            if (file.exists()) {
                file.delete();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            rulesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && result.toString().endsWith(".hhr")) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(result);
                File outputFile = createOutputFile();

                if (inputStream != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.close();
                    rulesPreference.edit().putString("filePath", outputFile.getAbsolutePath()).apply();
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

                    rules = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type rulesType = new TypeToken<ArrayList<Rule>>(){}.getType();
                    rules = gson.fromJson(jsonString, rulesType);
                    rulesAdapter.updateData(rules);
                    rulesPreference.edit().putBoolean("shouldExist", true).apply();
                    addRulesToFile();
                } else {
                    new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
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
            if (getFileExtension(result.toString()).equals("hhe") || getFileExtension(result.toString()).equals("hhm")) {
                new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle(getString(R.string.read_file_extension_not_valid_title))
                        .setMessage(getString(R.string.read_file_extension_not_valid_message_rules))
                        .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                        .show();
            } else {
                new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle(getString(R.string.read_file_extension_not_supported_title))
                        .setMessage(getString(R.string.read_file_extension_not_supported_message_exercises))
                        .setPositiveButton(getString(R.string.okay), ((dialog, which) -> dialog.cancel()))
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
        String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name) + ".hhr";
        return new File(destination);
    }
}