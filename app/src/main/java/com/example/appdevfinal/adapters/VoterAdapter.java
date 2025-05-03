package com.example.appdevfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.User;
import com.example.appdevfinal.models.Voter;
import java.util.ArrayList;
import java.util.List;

public class VoterAdapter extends RecyclerView.Adapter<VoterAdapter.VoterViewHolder> {
    private List<Voter> voters;

    public VoterAdapter(List<Voter> voters) {
        this.voters = voters;
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
        Voter voter = voters.get(position);
        holder.nameText.setText(voter.getName());
        holder.emailText.setText(voter.getEmail());
        holder.studentIdText.setText(voter.getStudentId());
        holder.statusText.setText(voter.hasVoted() ? "Has Voted" : "Not Voted");
    }

    @Override
    public int getItemCount() {
        return voters.size();
    }

    public void updateVoters(List<User> users) {
        List<Voter> voters = new ArrayList<>();
        for (User user : users) {
            if ("voter".equals(user.getRole())) {
                Voter voter = new Voter();
                voter.setId(user.getId());
                voter.setName(user.getName());
                voter.setEmail(user.getEmail());
                voter.setStudentId(user.getStudentId());
                voter.setRole(user.getRole());
                voter.setHasVoted(user.hasVoted());
                voters.add(voter);
            }
        }
        this.voters = voters;
        notifyDataSetChanged();
    }

    static class VoterViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView emailText;
        TextView studentIdText;
        TextView statusText;

        VoterViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.voterNameText);
            emailText = itemView.findViewById(R.id.voterEmailText);
            studentIdText = itemView.findViewById(R.id.studentNumberText);
            statusText = itemView.findViewById(R.id.votingStatusText);
        }
    }
}
