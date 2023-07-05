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
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Subtask;

import java.util.ArrayList;

public class SubtasksAdapter extends RecyclerView.Adapter<SubtasksAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Subtask> subtasks;
    private final OnLongClickInterface onLongClickInterface;

    public SubtasksAdapter(Context context, ArrayList<Subtask> subtasks, OnLongClickInterface onLongClickInterface) {
        this.context = context;
        this.subtasks = subtasks;
        this.onLongClickInterface = onLongClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context).inflate(R.layout.item_card_tasks, parent, false);
        return new ViewHolder(v, onLongClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subtask currentItem = subtasks.get(position);

        String name = currentItem.getSubTaskName();
        String info = currentItem.getSubTaskInfo();

        holder.subtaskTitle.setText(name);

        if (info == null) {
            holder.subtaskInfo.setVisibility(View.GONE);
        } else {
            holder.subtaskInfo.setVisibility(View.VISIBLE);
            holder.subtaskInfo.setText(info);
        }

    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView subtaskTitle;
        private final TextView subtaskInfo;

        public ViewHolder(@NonNull View itemView, OnLongClickInterface onLongClickInterface) {
            super(itemView);
            CardView exerciseHolderCard = itemView.findViewById(R.id.membersHolderCard);
            subtaskTitle = itemView.findViewById(R.id.exerciseName);
            subtaskInfo = itemView.findViewById(R.id.exerciseMemberName);
            itemView.findViewById(R.id.checkedImg).setVisibility(View.GONE);

            exerciseHolderCard.setOnLongClickListener(v -> {
                if (onLongClickInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        onLongClickInterface.onLongClick(position);
                    }
                    return true;
                }
                return false;
            });
        }
    }
}
