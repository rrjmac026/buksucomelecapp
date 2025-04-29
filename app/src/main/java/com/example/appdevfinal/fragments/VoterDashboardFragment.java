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

        loadUserDataAndVotingStatus();
        return view;
    }

    private void loadUserDataAndVotingStatus() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    // Update welcome text immediately
                    String name = document.getString("name");
                    String studentNumber = document.getString("studentNumber");
                    welcomeText.setText(String.format("Welcome, %s\nStudent Number: %s", 
                        name != null ? name : "Voter",
                        studentNumber != null ? studentNumber : "Not available"));

                    // Update voting status
                    Boolean hasVoted = document.getBoolean("hasVoted");
                    if (hasVoted != null && hasVoted) {
                        votingStatusText.setText("You have already cast your vote!");
                        votingStatusCard.setVisibility(View.VISIBLE);
                    } else {
                        votingStatusText.setText("You haven't voted yet. Please proceed to the Vote page.");
                        votingStatusCard.setVisibility(View.VISIBLE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("VoterDashboard", "Error loading user data", e);
                welcomeText.setText("Welcome\nUnable to load user data");
                votingStatusText.setText("Unable to check voting status");
                votingStatusCard.setVisibility(View.VISIBLE);
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserDataAndVotingStatus(); // Refresh data when returning to fragment
    }
}
