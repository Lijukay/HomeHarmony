package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.activity.MainActivity;
import com.lijukay.famecrew.activity.MembersActivity;
import com.lijukay.famecrew.adapter.MembersAdapter;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Member;

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

public class MembersFragment extends Fragment implements OnClickInterface, OnLongClickInterface {

    private MembersAdapter membersAdapter;
    private ArrayList<Member> members;
    private SharedPreferences membersPreference;
    private String destination;

    public MembersFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_member_overview, container, false);

        destination = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + getString(R.string.app_name) + ".hhm";

        membersPreference = requireContext().getSharedPreferences("Members", 0);

        RecyclerView rv = v.findViewById(R.id.memberExercises);
        rv.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        members = new ArrayList<>();

        if (new File(destination).exists()) {
            try {
                getFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (membersPreference.getBoolean("firstStart", true)){
            new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("About Members Overview")
                    .setMessage("This is an overview, that is made for a quicker view on member's tasks.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                    .setOnCancelListener(dialog -> membersPreference.edit().putBoolean("firstStart", false).apply())
                    .show();
        }

        membersAdapter = new MembersAdapter(requireContext(), members, this, this);
        rv.setAdapter(membersAdapter);

        return v;
    }

    public void addNewMember() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_member, (ViewGroup) getView(), false);
        TextInputLayout membersPrename = viewInflated.findViewById(R.id.memberPrename);
        TextInputLayout membersNickname = viewInflated.findViewById(R.id.memberNickname);

        viewInflated.findViewById(R.id.add_member_button).setOnClickListener(v -> {
            if (!Objects.requireNonNull(membersPrename.getEditText()).getText().toString().trim().equals("") && !Objects.requireNonNull(membersNickname.getEditText()).getText().toString().trim().equals("")) {
                if (!doesMemberAlreadyExist(Objects.requireNonNull(membersNickname.getEditText()).getText().toString().trim().toLowerCase(Locale.ROOT))) {
                    members.add(new Member(Objects.requireNonNull(membersPrename.getEditText()).getText().toString().trim(), Objects.requireNonNull(membersNickname.getEditText()).getText().toString().trim()));
                    membersAdapter.updateData(members);
                    addMembersToFile();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Nickname already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Name and nickname required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(viewInflated);

        dialog.show();
    }

    private void getFileContent() throws IOException {
        StringBuilder fileContent = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(destination)));
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        reader.close();


        members = new ArrayList<>();
        Gson gson = new Gson();
        String jsonString = fileContent.toString();
        Type exerciseType = new TypeToken<ArrayList<Member>>(){}.getType();

        members = gson.fromJson(jsonString, exerciseType);
    }

    public void addMembersToFile() {
        Gson gson = new Gson();
        if (members.size() != 0) {
            String jsonString = gson.toJson(members);
            //saveJSONAsFile(jsonString);
            ((MainActivity) requireActivity()).saveJsonAsFile(jsonString, destination);
        }
    }

    public ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null && ((MainActivity) requireActivity()).getFileExtension(result.toString()).equals("hhm")) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(result);

                File outputFile = new File(destination);

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

                    members = new ArrayList<>();
                    Gson gson = new Gson();
                    String jsonString = fileContent.toString();
                    Type membersType = new TypeToken<ArrayList<Member>>(){}.getType();
                    members = gson.fromJson(jsonString, membersType);
                    membersAdapter.updateData(members);
                    addMembersToFile();
                } else {
                    Toast.makeText(requireContext(), "Unable to read file", Toast.LENGTH_SHORT).show();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (result != null) {
            if (((MainActivity) requireActivity()).getFileExtension(result.toString()).equals("hhr") || ((MainActivity) requireActivity()).getFileExtension(result.toString()).equals("hhe")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Wrong file extension")
                        .setMessage("This is a Home Harmony file extension but you should choose a file that has the extension hhm.")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }  else if (!result.toString().equals("hhr") && !result.toString().equals("hhe") && !result.toString().endsWith("hhm")) {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("File extension not supported")
                        .setMessage("This is not a file extension that is supported by the app. Please choose a file which extension is hhm")
                        .setPositiveButton("Okay", (dialog, which) -> dialog.cancel())
                        .show();
            }
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
                    .setTitle("Members")
                    .setMessage("Create a file using \"New member\" or import a file using \"From file\". Please be aware that as for now, if you import a file, your current members will get deleted.")
                    .setPositiveButton("New member", (dialog, which) -> addNewMember())
                    .setNeutralButton("From file", (dialog, which) -> mGetContent.launch("application/octet-stream"))
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(int position, String type) {
        if (type.equals("card")) {
            Intent intent = new Intent(requireContext(), MembersActivity.class);
            intent.putExtra("MembersPreName", members.get(position).getPrename());
            intent.putExtra("MembersNickName", members.get(position).getNickname());
            startActivity(intent);
        }
    }

    public boolean doesMemberAlreadyExist(String nickname) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getNickname().trim().toLowerCase(Locale.ROOT).equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLongClick(int position) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_member, (ViewGroup) getView(), false);
        TextView nicknameTV = viewInflated.findViewById(R.id.memberNickname);
        TextInputLayout membersNameTIL = viewInflated.findViewById(R.id.membersName);

        Member member = members.get(position);

        String nickname = member.getNickname();
        if (nickname != null) {
            nicknameTV.setText(nickname);
        }

        if (member.getPrename() != null) {
            Objects.requireNonNull(membersNameTIL.getEditText()).setText(member.getPrename());
        }

        viewInflated.findViewById(R.id.buttonUpdate).setOnClickListener(view -> {
            String newName = Objects.requireNonNull(membersNameTIL.getEditText()).getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            Member newMember = new Member(newName, nickname);
            members.set(position, newMember);
            try {
                saveFileContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            membersAdapter.updateData(members);
            dialog.dismiss();

        });

        viewInflated.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            removeMember(position);
            dialog.dismiss();
        });

        dialog.setContentView(viewInflated);
        dialog.show();
    }

    public void removeMember(int position) {
        members.remove(position);
        updateMembers();
        try {
            saveFileContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFileContent() throws IOException {
        Gson gson = new Gson();
        String memberJson = gson.toJson(members);

        FileOutputStream fos = new FileOutputStream(destination);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(memberJson);
        bw.close();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateMembers() {
        membersAdapter.notifyDataSetChanged();
    }
}
