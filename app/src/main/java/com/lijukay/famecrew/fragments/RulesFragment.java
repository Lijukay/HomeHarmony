package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.activity.MainActivity;
import com.lijukay.famecrew.adapter.RulesAdapter;
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;
import com.lijukay.famecrew.objects.Rule;

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
import java.util.Locale;
import java.util.Objects;


public class RulesFragment extends Fragment implements OnLongClickInterface {

    private ArrayList<Rule> rules;
    private RulesAdapter rulesAdapter;
    private String rulesFilePath;

    public RulesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rulesFilePath = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.app_name) + ".hhr";
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rules_overview, container, false);

        RecyclerView rulesRV = v.findViewById(R.id.rulesRV);
        rulesRV.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        rules = new ArrayList<>();

        if (new File(rulesFilePath).exists()) {
            try {
                getFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        rulesAdapter = new RulesAdapter(requireContext(), rules, this);

        updateRules();

        rulesRV.setAdapter(rulesAdapter);

        return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addNewRule() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_rule, (ViewGroup) getView(), false);

        TextInputLayout ruleTitleEditText = viewInflated.findViewById(R.id.ruleTitleTF);
        TextInputLayout ruleMessageEditText = viewInflated.findViewById(R.id.ruleMessageTF);
        MaterialButton addButton = viewInflated.findViewById(R.id.buttonAdd);

        addButton.setOnClickListener(view -> {
            String ruleTitle = Objects.requireNonNull(ruleTitleEditText.getEditText()).getText().toString().trim();
            String ruleMessage = Objects.requireNonNull(ruleMessageEditText.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(ruleTitle) || TextUtils.isEmpty(ruleMessage)) {
                Toast.makeText(requireContext(), getString(R.string.must_include_t_and_r), Toast.LENGTH_SHORT).show();
                return;
            }

            Rule rule = new Rule(ruleTitle, ruleMessage);
            rules.add(rule);
            updateRules();
            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dialog.dismiss();
        });

        dialog.setContentView(viewInflated);
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRules() {
        rulesAdapter.notifyDataSetChanged();
    }

    private void getFileContent() throws IOException {
        StringBuilder fileContentBuilder = new StringBuilder();
        FileInputStream fis = new FileInputStream(rulesFilePath);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line;

        while ((line = br.readLine()) != null) {
            fileContentBuilder.append(line).append("\n");
        }

        br.close();

        Gson gson = new Gson();
        String jsonString = fileContentBuilder.toString();
        Type rulesType = new TypeToken<ArrayList<Rule>>(){}.getType();
        rules = gson.fromJson(jsonString, rulesType);

    }

    private void saveFileContent() throws IOException {
        Gson gson = new Gson();
        String rulesJson = gson.toJson(rules);

        FileOutputStream fos = new FileOutputStream(rulesFilePath);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(rulesJson);
        bw.close();
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && ((MainActivity) requireActivity()).getFileExtension(result.toString()).equals("hhr")) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(result);

                File outputFile = new File(rulesFilePath);

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

                    rules = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type rulesType = new TypeToken<ArrayList<Rule>>(){}.getType();
                    rules = gson.fromJson(jsonString, rulesType);
                    rulesAdapter.updateData(rules);
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
        } else  {
            Toast.makeText(requireContext(), getString(R.string.invalid_file), Toast.LENGTH_SHORT).show();
        }
    });
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.addItem).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addItem) {
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.rules))
                    .setMessage(getString(R.string.create_rule_dialog_message))
                    .setPositiveButton(getString(R.string.new_rule), (dialog, which) -> addNewRule())
                    .setNeutralButton(getString(R.string.from_file), (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show();
        }
        return false;
    }

    @Override
    public void onLongClick(int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_rule, (ViewGroup) getView(), false);
        TextInputLayout rulesTitle = viewInflated.findViewById(R.id.ruleTitle);
        TextInputLayout rulesRule = viewInflated.findViewById(R.id.rule);

        Rule rule = rules.get(position);
        Objects.requireNonNull(rulesTitle.getEditText()).setText(rule.getTitle());
        Objects.requireNonNull(rulesRule.getEditText()).setText(rule.getMessage());

        viewInflated.findViewById(R.id.buttonUpdate).setOnClickListener(v -> {

            String newTitle = Objects.requireNonNull(rulesTitle.getEditText()).getText().toString().trim();
            String newRulesRule = Objects.requireNonNull(rulesRule.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(newTitle)) {
                Toast.makeText(requireContext(), getString(R.string.must_include_t_and_r), Toast.LENGTH_SHORT).show();
                return;
            }

            Rule newRule = new Rule(newTitle, newRulesRule);

            rules.set(position, newRule);

            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            rulesAdapter.updateData(rules);
            dialog.dismiss();
        });

        viewInflated.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            removeRule(position);
            dialog.dismiss();
        });

        dialog.setContentView(viewInflated);
        dialog.show();
    }

    public void removeRule(int position) {
        rules.remove(position);
        updateRules();
        try {
            saveFileContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}