package com.lijukay.famecrew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lijukay.famecrew.R;
import com.lijukay.famecrew.objects.Subtask;

import java.util.ArrayList;

public class SubtasksAdapterSimpleItem extends RecyclerView.Adapter<SubtasksAdapterSimpleItem.ViewHolder> {
    private final Context context;
    private ArrayList<Subtask> subtasks;

    public SubtasksAdapterSimpleItem(Context context, ArrayList<Subtask> subtasks) {
        this.context = context;
        this.subtasks = subtasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context).inflate(R.layout.item_simple_text, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subtask currentItem = subtasks.get(position);

        String name = currentItem.getSubTaskName();
        holder.itemText.setText(name);
    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.textView);
        }
    }
}
