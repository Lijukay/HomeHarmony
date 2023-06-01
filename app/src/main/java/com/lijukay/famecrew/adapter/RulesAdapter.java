package com.lijukay.famecrew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lijukay.famecrew.R;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Rule;

import java.util.ArrayList;

public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Rule> rules;
    private final OnClickInterface onClickInterface;

    public RulesAdapter(Context context, ArrayList<Rule> rules, OnClickInterface onClickInterface) {
        this.context = context;
        this.rules = rules;
        this.onClickInterface = onClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context).inflate(R.layout.rules_card, parent, false);
        return new ViewHolder(v, onClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rule currentItem = rules.get(position);

        String title = "";
        String message = "";
        if (currentItem.getTitle() != null) {
            title = currentItem.getTitle();
        }
        if (currentItem.getMessage()!=null){
            message = currentItem.getMessage();
        }

        if (!title.equals("") && !message.equals("")) {
            holder.rulesTitle.setText(title);
            holder.rulesMessage.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Rule> rules) {
        this.rules = rules;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final CardView rulesCardHolder;
        public final TextView rulesTitle;
        public final TextView rulesMessage;

        public ViewHolder(@NonNull View itemView, OnClickInterface onClickInterface) {
            super(itemView);
            rulesCardHolder = itemView.findViewById(R.id.rulesCardHolder);
            rulesTitle = itemView.findViewById(R.id.rulesTitle);
            rulesMessage = itemView.findViewById(R.id.rulesMessage);
        }
    }
}