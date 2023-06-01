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

    ExtendedFloatingActionButton efab;
    MaterialToolbar title;
    ArrayList<Rule> rules;
    RulesAdapter rulesAdapter;
    RecyclerView rulesRV;
    SharedPreferences rulesPreference;
    Context context;
    String filePathString;
    boolean firstStartFragment;

    public RulesOverview(MaterialToolbar title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rules_overview, container, false);

        context = requireContext();

        rulesPreference = context.getSharedPreferences("Rules", 0);
        filePathString = rulesPreference.getString("filePath", null);
        firstStartFragment = rulesPreference.getBoolean("firstStart", true);

        title.setTitle("Rulebook");

        efab = v.findViewById(R.id.addRule);

        rulesRV = v.findViewById(R.id.rulesRV);
        rulesRV.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        rules = new ArrayList<>();

        if (filePathString != null) {
            getFileContent(new File(filePathString));
        } else if (firstStartFragment){
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Tutorial for Rules")
                    .setMessage("Rules include rules (ofc) but since you have not created a rule, there is no file yet. To create a rule, simply press on \"Add rule\" and choose whether you like to create a new rule or to add rules from a file. Then, a rule will be created.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
            rulesPreference.edit().putBoolean("firstStart", false).apply();
        } else {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("File not found")
                    .setMessage("There is no file called \"DocumentsFameCrew-Rules.json\". Either you have not create a rule yet, or this unexpected. However, to create a new rule, simply press on \"Add rule\"")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
        }

        rulesAdapter = new RulesAdapter(requireContext(), rules, null);

        rulesRV.setAdapter(rulesAdapter);

        efab.setOnClickListener(v1 ->
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                .setTitle("Choose from file or make own?")
                .setMessage("If you have a file, that contains rules, feel free to implement it. Your current rules won't get deleted.")
                .setPositiveButton("New Rule", (dialog, which) -> addNewRule())
                .setNeutralButton("Open from file", (dialog, which) -> mGetContent.launch("application/octet-stream"))
                .show());

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
    private void addNewRule() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);

        builder.setTitle("New rule");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.add_rule_dialog, (ViewGroup) getView(), false);

        TextInputLayout ruleTitle = viewInflated.findViewById(R.id.ruleTitleTF);
        TextInputLayout ruleMessage = viewInflated.findViewById(R.id.ruleMessageTF);

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", (dialog, which) -> {
            if (!Objects.requireNonNull(ruleTitle.getEditText()).getText().toString().trim().equals("") && !Objects.requireNonNull(ruleMessage.getEditText()).getText().toString().trim().equals("")) {
                rules.add(new Rule(ruleTitle.getEditText().getText().toString().trim(), ruleMessage.getEditText().getText().toString().trim()));
                rulesAdapter.notifyDataSetChanged();
                addRulesToFile();
            } else {
                Toast.makeText(requireContext(), "Both, title and rule, must include text", Toast.LENGTH_SHORT).show();
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
            saveJsonAsFile(requireContext(), jsonString);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveJsonAsFile(Context context, String jsonString) {
        try {
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + getString(R.string.app_name) + "-Rules.famecrew";
            File file = new File(destination);

            if (file.exists()) {
                file.delete();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            Toast.makeText(context, "File was saved successfully", Toast.LENGTH_SHORT).show();
            rulesPreference.edit().putString("filePath", file.getAbsolutePath()).apply();
        } catch (IOException e) {
            new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Unable to create file")
                    .setMessage("There was an error creating a file. Please try again later.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .show();
            throw new RuntimeException(e);
        }
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && result.toString().endsWith("-Rules.famecrew")) {
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
                    addRulesToFile();
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
            } else if (!result.toString().endsWith("-Rules.famecrew")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Almost")
                        .setMessage("Please make sure, your file ends with \"-Rules.famecrew\".")
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
        String destination = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + getString(R.string.app_name) + "-Rules.famecrew";
        return new File(destination);
    }
}