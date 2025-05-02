package com.example.appdevfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.Candidate;
import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder> {
    private List<Candidate> candidates;
    private OnCandidateClickListener listener;

    public interface OnCandidateClickListener {
        void onCandidateClick(Candidate candidate);
        void onEditClick(Candidate candidate);
        void onDeleteClick(Candidate candidate);
    }

    public CandidateAdapter(List<Candidate> candidates, OnCandidateClickListener listener) {
        this.candidates = candidates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CandidateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_candidate, parent, false);
        return new CandidateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CandidateViewHolder holder, int position) {
        Candidate candidate = candidates.get(position);
        holder.candidateNameText.setText(candidate.getName());
        holder.candidatePositionText.setText(candidate.getPosition());
        holder.candidatePartyText.setText(candidate.getPartyList());
        
        // Set click listeners for buttons
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(candidate);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(candidate);
            }
        });
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    static class CandidateViewHolder extends RecyclerView.ViewHolder {
        TextView candidateNameText;
        TextView candidatePositionText;
        TextView candidatePartyText;
        ImageButton editButton;
        ImageButton deleteButton;

        CandidateViewHolder(View itemView) {
            super(itemView);
            candidateNameText = itemView.findViewById(R.id.candidateNameText);
            candidatePositionText = itemView.findViewById(R.id.candidatePositionText);
            candidatePartyText = itemView.findViewById(R.id.candidatePartyText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
