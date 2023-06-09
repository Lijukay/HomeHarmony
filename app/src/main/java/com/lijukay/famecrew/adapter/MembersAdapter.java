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
import com.lijukay.famecrew.interfaces.OnClickInterface;
import com.lijukay.famecrew.objects.Member;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private final Context context;
    public ArrayList<Member> members;
    public OnClickInterface onClickInterface;

    public MembersAdapter(Context context, ArrayList<Member> members, OnClickInterface onClickInterface){
        this.context = context;
        this.members = members;
        this.onClickInterface = onClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.object_item, parent, false);
        return new ViewHolder(v, onClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member currentItem = members.get(position);

        String prename = currentItem.getPrename();
        String nickname = currentItem.getNickname();

        holder.member.setText(prename + " " + " (" + nickname + ")");
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView member;

        public ViewHolder(@NonNull View itemView, OnClickInterface onClickInterface) {
            super(itemView);
            member = itemView.findViewById(R.id.objectTextHolder);

            member.setOnClickListener(v -> {
                if (onClickInterface != null){
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION){
                        onClickInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
