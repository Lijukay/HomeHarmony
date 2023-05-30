package com.lijukay.famecrew.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.lijukay.famecrew.R;


public class RulesOverview extends Fragment {

    // TODO: 30.05.2023 Add possibility to write rules
    // TODO: 30.05.2023 Add possibility to share a file full of rules
    // TODO: 30.05.2023 Show rules from file
    // TODO: 30.05.2023 Create card item for rules
    // TODO: 30.05.2023 Create rule object (including title and rulemessage)
    // TODO: 30.05.2023 Create rule object adapter

    ExtendedFloatingActionButton efab;
    MaterialToolbar title;

    public RulesOverview(MaterialToolbar title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rules_overview, container, false);

        title.setTitle("Rulebook");

        efab = v.findViewById(R.id.addRule);

        efab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 30.05.2023 add rule
            }
        });

        return v;
    }
}