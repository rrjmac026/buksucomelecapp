package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import java.util.Calendar;
import java.util.List;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.appdevfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class VoterDashboardFragment extends Fragment {
    private TextView welcomeText, votingStatusText, greetingText;
    private MaterialCardView votingStatusCard, voteNowCard, viewVotingDetailsCard;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voter_dashboard, container, false);
        
        initializeViews(view);
        setGreeting();
        setupClickListeners();
        loadUserDataAndVotingStatus();
        
        return view;
    }

    private void initializeViews(View view) {
        welcomeText = view.findViewById(R.id.welcomeText);
        greetingText = view.findViewById(R.id.greetingText);
        votingStatusText = view.findViewById(R.id.votingStatusText);
        votingStatusCard = view.findViewById(R.id.votingStatusCard);
        voteNowCard = view.findViewById(R.id.voteNowCard);
        viewVotingDetailsCard = view.findViewById(R.id.viewVotingDetailsCard);
        db = FirebaseFirestore.getInstance();
    }

    private void setGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;
        
        if(timeOfDay < 12){
            greeting = "Good Morning";
        } else if(timeOfDay < 16){
            greeting = "Good Afternoon";
        } else if(timeOfDay < 21){
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }
        
        greetingText.setText(greeting);
    }

    private void setupClickListeners() {
        voteNowCard.setOnClickListener(v -> {
            // Navigate to Vote Fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new VoteFragment())
                .addToBackStack(null)
                .commit();
        });

        viewVotingDetailsCard.setOnClickListener(v -> {
            showVotingDetails();
        });
    }

    private void showVotingDetails() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("votes")
            .whereEqualTo("voterId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    com.google.firebase.firestore.DocumentSnapshot voteDoc = queryDocumentSnapshots.getDocuments().get(0);
                    
                    // Inflate custom layout
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_voting_details, null);
                    
                    // Set president
                    TextView presidentText = dialogView.findViewById(R.id.presidentText);
                    presidentText.setText(voteDoc.getString("president"));
                    
                    // Set vice president
                    TextView vicePresidentText = dialogView.findViewById(R.id.vicePresidentText);
                    vicePresidentText.setText(voteDoc.getString("vicePresident"));
                    
                    // Set senators
                    LinearLayout senatorsContainer = dialogView.findViewById(R.id.senatorsContainer);
                    List<String> senators = (List<String>) voteDoc.get("senators");
                    if (senators != null) {
                        for (String senator : senators) {
                            TextView senatorText = new TextView(requireContext());
                            senatorText.setText("• " + senator);
                            senatorText.setTextSize(16);
                            senatorText.setPadding(0, 8, 0, 8);
                            senatorText.setTextColor(getResources().getColor(R.color.white));
                            senatorsContainer.addView(senatorText);
                        }
                    }

                    // Show custom dialog with styled button
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
                        .setView(dialogView)
                        .setPositiveButton("Close", null);
                    
                    // Create and style the dialog using androidx AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(dialogInterface -> {
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.white));
                        positiveButton.setBackgroundColor(getResources().getColor(R.color.buksu_deep_purple));
                        positiveButton.setPadding(40, 0, 40, 0); // Add some padding
                    });
                    dialog.show();
                } else {
                    Toast.makeText(requireContext(), "No voting record found", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error loading voting details: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void loadUserDataAndVotingStatus() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String name = document.getString("name");
                    welcomeText.setText(String.format("Welcome, %s", 
                        name != null ? name : "Voter"));

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
