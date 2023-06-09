package com.lijukay.famecrew.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.activity.MainActivity;
import com.lijukay.famecrew.activity.OpenFileActivity;
import com.lijukay.famecrew.adapter.MembersAdapter;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Member;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class CFPage1 extends Fragment implements OnClickInterface {

    private int nextButtonID;
    private int cancelButtonID;
    private ArrayList<Member> members;
    private MembersAdapter membersAdapter;
    private TextInputLayout prenameInput;
    private TextInputLayout nicknameInput;

    public CFPage1() {
    }

    public static CFPage1 newInstance (int nextButtonID, int cancelButtonID) {
        CFPage1 fragment = new CFPage1();
        Bundle args = new Bundle();
        args.putInt("nextButtonID", nextButtonID);
        args.putInt("cancelButtonID", cancelButtonID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nextButtonID = getArguments().getInt("nextButtonID");
            cancelButtonID = getArguments().getInt("cancelButtonID");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_c_f_page1, container, false);

        RecyclerView membersList = v.findViewById(R.id.membersList);
        MaterialButton addButton = v.findViewById(R.id.add);
        prenameInput = v.findViewById(R.id.memberPrename);
        nicknameInput = v.findViewById(R.id.memberNickname);

        MaterialButton next = requireActivity().findViewById(nextButtonID);
        MaterialButton cancel = requireActivity().findViewById(cancelButtonID);

        membersList.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        members = new ArrayList<>();


        membersAdapter = new MembersAdapter(requireContext(), members, this);

        if (next != null) {
            next.setEnabled(isEnableAllowed());
        }

        membersList.setAdapter(membersAdapter);

        addButton.setOnClickListener(v1 -> {
            String prename = Objects.requireNonNull(prenameInput.getEditText()).getText().toString();
            String nickname = Objects.requireNonNull(nicknameInput.getEditText()).getText().toString();

            addToArrayAndRefresh(prename, nickname);
            if (next != null) {
                next.setEnabled(isEnableAllowed());
            }
        });

        if(cancel != null) {
            requireActivity().finish();
            cancel.setText(getString(R.string.cancel));
            cancel.setOnClickListener(v13 -> startActivity(new Intent(requireContext(), OpenFileActivity.class)));
        }

        if (next != null) {
            next.setText(getString(R.string.next));
            next.setOnClickListener(v12 -> {
                addMembersToFile();
                startActivity(new Intent(requireContext(), MainActivity.class));
                requireActivity().finish();
                requireContext().getSharedPreferences("firstStart", 0).edit().putBoolean("firstStart", false).apply();
            });
        }

        return v;
    }

    private boolean isEnableAllowed() {
        return membersAdapter.getItemCount() != 0;
    }

    private void addMembersToFile() {
        Gson gson = new Gson();
        if (members.size() != 0) {
            String jsonString = gson.toJson(members);
            saveJsonAsFile(requireContext(), jsonString);
        }
    }

    private void saveJsonAsFile(Context context, String jsonString) {
        try {
            String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name) + ".hhm";
            File file = new File(destination);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            context.getSharedPreferences("Members", 0).edit().putString("filePath", file.getAbsolutePath()).apply();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addToArrayAndRefresh(String prename, String nickname) {
        if (!isAlreadyInList(nickname)) {
            members.add(new Member(prename, nickname));
            membersAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_nickname), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isAlreadyInList(String nickname) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onItemClick(int position) {

    }
}