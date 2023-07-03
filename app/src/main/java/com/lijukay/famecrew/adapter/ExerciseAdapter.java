package com.lijukay.famecrew.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lijukay.famecrew.R;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.util.ArrayList;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Exercise> exercises;
    private final OnClickInterface onClickInterface;
    private final OnLongClickInterface onLongClickInterface;

    public ExerciseAdapter(Context context, ArrayList<Exercise> exercises, OnClickInterface onClickInterface, OnLongClickInterface onLongClickInterface) {
        this.context = context;
        this.exercises = exercises;
        this.onClickInterface = onClickInterface;
        this.onLongClickInterface = onLongClickInterface;
    }

    @NonNull
    @Override
    public ExerciseAdapter.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context).inflate(R.layout.item_card_tasks, parent, false);
        return new ViewHolder(v, onClickInterface, onLongClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExerciseAdapter.ViewHolder holder, int position) {
        Exercise currentItem = exercises.get(position);

        String exerciseName = currentItem.getExName();
        Member member = currentItem.getMember();
        boolean isDone = currentItem.isDone();

        holder.exerciseNameTextView.setText(exerciseName);
        if (member != null) {
            holder.exerciseMemberTextView.setVisibility(View.VISIBLE);
            holder.exerciseMemberTextView.setText(member.getPrename() + " (" + member.getNickname() + ")"); // TODO: 02.07.2023 Add to String.xml
        } else {
            holder.exerciseMemberTextView.setVisibility(View.GONE);
        }

        if (isDone) {
            holder.checkedImg.setVisibility(View.VISIBLE);
        } else {
            holder.checkedImg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final CardView exerciseHolderCard;
        public final TextView exerciseNameTextView;
        public final TextView exerciseMemberTextView;
        public final ImageView checkedImg;

        public ViewHolder(@NonNull View itemView, OnClickInterface onClickInterface, OnLongClickInterface onLongClickInterface) {
            super(itemView);
            exerciseHolderCard = itemView.findViewById(R.id.membersHolderCard);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseName);
            exerciseMemberTextView = itemView.findViewById(R.id.exerciseMemberName);
            checkedImg = itemView.findViewById(R.id.checkedImg);

            exerciseHolderCard.setOnClickListener(v -> {
                if (onClickInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        onClickInterface.onItemClick(position, "card");
                    }
                }
            });

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

            checkedImg.setOnClickListener(v -> {
                if (onClickInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        onClickInterface.onItemClick(position, "img");
                    }
                }
            });
        }
    }
}