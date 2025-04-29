package com.example.appdevfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.Candidate;
import java.util.List;
import java.util.Map;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {
    private List<?> items; // Can be List<Candidate> or List<Map<String, Object>>
    private int selectedPosition = -1;
    private boolean isVotingMode;
    private OnCandidateClickListener listener;

    public interface OnCandidateClickListener {
        void onCandidateEdit(Candidate candidate);
        void onCandidateDelete(Candidate candidate);
    }

    public CandidateAdapter(List<?> items, boolean isVotingMode) {
        this.items = items;
        this.isVotingMode = isVotingMode;
    }

    public CandidateAdapter(List<Candidate> items, OnCandidateClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.isVotingMode = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isVotingMode ? R.layout.item_candidate : R.layout.item_candidate_manage;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isVotingMode) {
            Map<String, Object> candidate = (Map<String, Object>) items.get(position);
            holder.candidateName.setText((String) candidate.get("name"));
            holder.candidateParty.setText((String) candidate.get("partyList"));
            if (holder.radioButton != null) {
                holder.radioButton.setChecked(position == selectedPosition);
                holder.itemView.setOnClickListener(v -> {
                    selectedPosition = holder.getAdapterPosition();
                    notifyDataSetChanged();
                });
            }
        } else {
            Candidate candidate = (Candidate) items.get(position);
            holder.candidateName.setText(candidate.getName());
            holder.candidateParty.setText(candidate.getPartyList());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCandidateEdit(candidate);
            });
            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    if (listener != null) listener.onCandidateDelete(candidate);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Object getSelectedItem() {
        return selectedPosition != -1 ? items.get(selectedPosition) : null;
    }

    public Map<String, Object> getSelectedCandidate() {
        if (selectedPosition != -1 && isVotingMode) {
            return (Map<String, Object>) items.get(selectedPosition);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView candidateName, candidateParty;
        RadioButton radioButton;
        View deleteButton;

        ViewHolder(View view) {
            super(view);
            candidateName = view.findViewById(R.id.candidateName);
            candidateParty = view.findViewById(R.id.candidateParty);
            radioButton = view.findViewById(R.id.radioButton);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
