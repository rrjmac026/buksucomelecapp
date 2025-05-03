package com.example.appdevfinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.LoginActivity;  // Fixed import path
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class VoterManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView voterCountText;
    private View loadingProgressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private VoterAdapter adapter;
    private ListenerRegistration votersListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voter_management, container, false);
        
        recyclerView = view.findViewById(R.id.votersRecyclerView);
        voterCountText = view.findViewById(R.id.voterCountText);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        adapter = new VoterAdapter();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        loadVoters();
        
        return view;
    }

    private void loadVoters() {
        if (!isAdded() || auth.getCurrentUser() == null) {
            // If not authenticated, redirect to login
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
        
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        votersListener = db.collection("users")
            .whereEqualTo("role", "voter")
            .addSnapshotListener((value, error) -> {
                if (!isAdded()) return;

                if (error != null) {
                    if (error.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        // Handle authentication errors
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } else if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading voters: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                    loadingProgressBar.setVisibility(View.GONE);
                    return;
                }

                List<VoterItem> voters = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    VoterItem voter = new VoterItem(
                        doc.getId(),
                        doc.getString("name"),
                        doc.getString("email"),
                        doc.getString("studentNumber"),
                        doc.getBoolean("hasVoted")
                    );
                    voters.add(voter);
                }
                
                if (isAdded()) {
                    adapter.updateVoters(voters);
                    voterCountText.setText("Total Voters: " + voters.size());
                    loadingProgressBar.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (votersListener != null) {
            votersListener.remove();
        }
    }

    private void removeVoter(String voterId) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Voter")
            .setMessage("Are you sure you want to remove this voter?")
            .setPositiveButton("Remove", (dialog, which) -> {
                db.collection("users").document(voterId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), 
                        "Voter removed successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), 
                        "Failed to remove voter: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private static class VoterItem {
        String id;
        String name;
        String email;
        String studentNumber;
        Boolean hasVoted;

        VoterItem(String id, String name, String email, String studentNumber, Boolean hasVoted) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.studentNumber = studentNumber;
            this.hasVoted = hasVoted != null ? hasVoted : false; // Provide default value if null
        }
    }

    private class VoterAdapter extends RecyclerView.Adapter<VoterAdapter.VoterViewHolder> {
        private List<VoterItem> voters = new ArrayList<>();

        @NonNull
        @Override
        public VoterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voter, parent, false);
            return new VoterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VoterViewHolder holder, int position) {
            VoterItem voter = voters.get(position);
            holder.nameText.setText(voter.name != null ? voter.name : "N/A");
            holder.emailText.setText(voter.email != null ? voter.email : "N/A");
            holder.studentNumberText.setText("Student #: " + (voter.studentNumber != null ? voter.studentNumber : "N/A"));
            // Add null check for hasVoted
            holder.votingStatusText.setText(voter.hasVoted != null && voter.hasVoted ? "Has Voted" : "Has Not Voted");
            holder.removeButton.setOnClickListener(v -> removeVoter(voter.id));
        }

        @Override
        public int getItemCount() {
            return voters.size();
        }

        void updateVoters(List<VoterItem> newVoters) {
            voters = newVoters;
            notifyDataSetChanged();
        }

        class VoterViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView emailText;
            TextView studentNumberText;
            TextView votingStatusText;
            MaterialButton removeButton;

            VoterViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.voterNameText);
                emailText = itemView.findViewById(R.id.voterEmailText);
                studentNumberText = itemView.findViewById(R.id.studentNumberText);
                votingStatusText = itemView.findViewById(R.id.votingStatusText);
                removeButton = itemView.findViewById(R.id.removeVoterButton);
            }
        }
    }
}
