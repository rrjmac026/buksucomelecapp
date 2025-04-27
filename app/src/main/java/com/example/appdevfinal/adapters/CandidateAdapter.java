package com.example.appdevfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.textName.setText(candidate.getName());
        holder.textPosition.setText(candidate.getPosition());
        holder.textPartyList.setText(candidate.getPartyList());

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(candidate));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(candidate));
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    static class CandidateViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPosition, textPartyList;
        Button buttonEdit, buttonDelete;

        CandidateViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPosition = itemView.findViewById(R.id.textPosition);
            textPartyList = itemView.findViewById(R.id.textPartyList);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
