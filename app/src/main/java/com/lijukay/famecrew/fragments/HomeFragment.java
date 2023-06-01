package com.lijukay.famecrew.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.lijukay.famecrew.R;

public class HomeFragment extends Fragment {

    MaterialToolbar toolbar;
    SharedPreferences settingsPreference;
    String user;

    public HomeFragment(MaterialToolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        settingsPreference = requireContext().getSharedPreferences("Settings", 0);
        user = settingsPreference.getString("user", "user");

        toolbar.setTitle("Welcome " + user);

        return v;
    }
}