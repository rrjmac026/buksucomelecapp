package com.example.appdevfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.User;
import java.util.List;

public class VoterAdapter extends RecyclerView.Adapter<VoterAdapter.VoterViewHolder> {
    private List<User> users;

    public VoterAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public VoterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voter, parent, false);
        return new VoterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoterViewHolder holder, int position) {
        User user = users.get(position);
        holder.nameText.setText(user.getName() != null ? user.getName() : "N/A");
        holder.emailText.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        holder.studentIdText.setText(user.getStudentId() != null ? user.getStudentId() : "N/A");
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateVoters(List<User> newUsers) {
        users = newUsers;
        notifyDataSetChanged();
    }

    static class VoterViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, studentIdText;

        VoterViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.voterName);
            emailText = itemView.findViewById(R.id.voterEmail);
            studentIdText = itemView.findViewById(R.id.voterStudentId);
        }
    }
}
