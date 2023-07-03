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
import com.lijukay.famecrew.interfaces.OnLongClickInterface;
import com.lijukay.famecrew.objects.Member;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private final Context context;
    public ArrayList<Member> members;
    public OnClickInterface onClickInterface;
    public OnLongClickInterface onLongClickInterface;

    public MembersAdapter(Context context, ArrayList<Member> members, OnClickInterface onClickInterface, OnLongClickInterface onLongClickInterface){
        this.context = context;
        this.members = members;
        this.onClickInterface = onClickInterface;
        this.onLongClickInterface = onLongClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_card_members, parent, false);
        return new ViewHolder(v, onClickInterface, onLongClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member currentItem = members.get(position);

        String prename = currentItem.getPrename();
        String nickname = currentItem.getNickname();

        holder.member.setText(prename);
        holder.nickname.setText(nickname);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView member;
        private final TextView nickname;

        public ViewHolder(@NonNull View itemView, OnClickInterface onClickInterface, OnLongClickInterface onLongClickInterface) {
            super(itemView);
            CardView membersCardViewHolder = itemView.findViewById(R.id.membersHolderCard);
            member = itemView.findViewById(R.id.membersPrename);
            nickname = itemView.findViewById(R.id.membersNickname);

            membersCardViewHolder.setOnClickListener(v -> {
                if (onClickInterface != null){
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION){
                        onClickInterface.onItemClick(position, "card");
                    }
                }
            });

            membersCardViewHolder.setOnLongClickListener(v -> {
                if (onLongClickInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        onLongClickInterface.onLongClick(position);
                    }
                }

                return false;
            });

        }
    }
}
