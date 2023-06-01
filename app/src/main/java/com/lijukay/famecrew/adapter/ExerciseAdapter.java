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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lijukay.famecrew.R;
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Exercise;
import com.lijukay.famecrew.objects.Member;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Exercise> exercises;
    private final OnClickInterface onClickInterface;

    public ExerciseAdapter(Context context, ArrayList<Exercise> exercises, OnClickInterface onClickInterface) {
        this.context = context;
        this.exercises = exercises;
        this.onClickInterface = onClickInterface;
    }

    @NonNull
    @Override
    public ExerciseAdapter.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context).inflate(R.layout.exercises_no_preview_item, parent, false);
        return new ViewHolder(v, onClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExerciseAdapter.ViewHolder holder, int position) {
        Exercise currentItem = exercises.get(position);

        String exerciseName = currentItem.getExName();
        Member member = currentItem.getMember();

        holder.exerciseNameTextView.setText(exerciseName);
        if (!member.getNickname().equals("") && !getMember(member.getNickname()).getNickname().equals("")) {
            holder.exerciseMemberTextView.setText(member.getPrename() + " (" + member.getNickname() + ")");
        } else {
            holder.exerciseMemberTextView.setVisibility(View.GONE);
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

        public ViewHolder(@NonNull View itemView, OnClickInterface onClickInterface) {
            super(itemView);
            exerciseHolderCard = itemView.findViewById(R.id.exerciseHolderCard);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseName);
            exerciseMemberTextView = itemView.findViewById(R.id.exerciseMemberName);
        }
    }

    private Member getMember(String memberNickname) {

        if (context.getSharedPreferences("Members", 0).getString("filePath", null) != null){
            File file = new File(context.getSharedPreferences("Members", 0).getString("filePath", null));
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

            Gson gson = new Gson();
            String jsonString = fileContent.toString();

            Type memberType = new TypeToken<ArrayList<Member>>(){}.getType();
            ArrayList<Member> members = gson.fromJson(jsonString, memberType);

            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).getNickname().equals(memberNickname)){
                    return members.get(i);
                }
            }
        }
        return new Member("", "");
    }


}