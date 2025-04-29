package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class VoterDashboardFragment extends Fragment {
    private TextView welcomeText;
    private TextView votingStatusText;
    private CardView votingStatusCard;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voter_dashboard, container, false);
        
        welcomeText = view.findViewById(R.id.welcomeText);
        votingStatusText = view.findViewById(R.id.votingStatusText);
        votingStatusCard = view.findViewById(R.id.votingStatusCard);
        db = FirebaseFirestore.getInstance();

        loadUserData();
        checkVotingStatus();
        
        return view;
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String studentNumber = document.getString("studentNumber"); // Changed from studentId
                        String welcomeMessage = String.format("Welcome, %s\nStudent Number: %s", 
                            name != null ? name : "Voter",
                            studentNumber != null ? studentNumber : "Not available");
                        welcomeText.setText(welcomeMessage);
                        
                        // Debug log
                        Log.d("VoterDashboard", "Document data: " + document.getData());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("VoterDashboard", "Error fetching user data", e);
                    welcomeText.setText("Welcome, Voter\nStudent Number: Not available");
                });
        }
    }

    private void checkVotingStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("votes").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String candidateName = document.getString("candidateName");
                        String timestamp = document.getString("timestamp");
                        String voteStatus = "You have voted for: " + candidateName + 
                                         "\nVoted on: " + timestamp;
                        votingStatusText.setText(voteStatus);
                        votingStatusCard.setVisibility(View.VISIBLE);
                    } else {
                        votingStatusText.setText("You haven't voted yet. Please proceed to the Vote page.");
                        votingStatusCard.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("VoterDashboard", "Error checking voting status", e);
                });
        }
    }
}
