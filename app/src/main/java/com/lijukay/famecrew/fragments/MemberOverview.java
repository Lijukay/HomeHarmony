package com.lijukay.famecrew.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.activity.MembersOverview;
import com.lijukay.famecrew.adapter.MembersAdapter;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MemberOverview extends Fragment implements OnClickInterface {

    private final MaterialToolbar materialToolbar;
    private ArrayList<Member> members;

    public MemberOverview(MaterialToolbar materialToolbar) {
        this.materialToolbar = materialToolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_member_overview, container, false);

        Context context = requireContext();

        SharedPreferences membersPreference = context.getSharedPreferences("Members", 0);
        boolean firstStart = membersPreference.getBoolean("firstStart", true);
        String filePath = membersPreference.getString("filePath", null);

        materialToolbar.setTitle(getString(R.string.members));
        RecyclerView rv = v.findViewById(R.id.memberExercises);

        rv.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));

        members = new ArrayList<>();

        if (filePath != null) {
            getFileContent(new File(membersPreference.getString("filePath", null)));
            if (members.size() == 0) {
                members.add(new Member(getString(R.string.no_member), getString(R.string.no_member)));
            }
        } else if (firstStart){
            new MaterialAlertDialogBuilder(context, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle(getString(R.string.first_start_dialog_title_members))
                    .setMessage(getString(R.string.first_start_dialog_message_members))
                    .setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.cancel())
                    .setOnCancelListener(dialog -> membersPreference.edit().putBoolean("firstStart", false).apply())
                    .show();
            members.add(new Member(getString(R.string.no_member), getString(R.string.no_member)));
        } else {
            members.add(new Member(getString(R.string.no_member), getString(R.string.no_member)));
        }

        MembersAdapter membersAdapter = new MembersAdapter(context, members, this);
        rv.setAdapter(membersAdapter);

        return v;
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

        members = new ArrayList<>();
        Gson gson = new Gson();
        String jsonString = fileContent.toString();
        Type exerciseType = new TypeToken<ArrayList<Member>>(){}.getType();

        members = gson.fromJson(jsonString, exerciseType);

        // TODO: 04.06.2023 Add possibility to add members 
    }



    @Override
    public void onItemClick(int position) {
        Log.e("ff", "true");
        Intent intent = new Intent(requireContext(), MembersOverview.class);
        intent.putExtra("MembersPreName", members.get(position).getPrename());
        intent.putExtra("MembersNickName", members.get(position).getNickname());
        startActivity(intent);
    }
}