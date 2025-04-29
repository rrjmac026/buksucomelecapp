package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.Candidate;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteFragment extends Fragment {
    private RadioGroup presidentGroup, vpGroup;
    private LinearLayout senatorsGroup;
    private Button submitVoteButton;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        presidentGroup = view.findViewById(R.id.presidentGroup);
        vpGroup = view.findViewById(R.id.vpGroup);
        senatorsGroup = view.findViewById(R.id.senatorsGroup);
        submitVoteButton = view.findViewById(R.id.submitVoteButton);

        loadCandidates();
        submitVoteButton.setOnClickListener(v -> validateAndSubmitVote());
        
        return view;
    }

    private void loadCandidates() {
        // Clear existing options
        presidentGroup.removeAllViews();
        vpGroup.removeAllViews();
        senatorsGroup.removeAllViews();

        db.collection("candidates")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Candidate candidate = document.toObject(Candidate.class);
                    if (candidate != null) {
                        switch (candidate.getPosition().toLowerCase()) {
                            case "president":
                                addRadioButton(presidentGroup, candidate);
                                break;
                            case "vice president":
                                addRadioButton(vpGroup, candidate);
                                break;
                            case "senator":
                                addCheckBox(senatorsGroup, candidate);
                                break;
                        }
                    }
                }
            })
            .addOnFailureListener(e -> showError("Failed to load candidates: " + e.getMessage()));
    }

    private void addRadioButton(RadioGroup group, Candidate candidate) {
        RadioButton rb = new RadioButton(requireContext());
        rb.setLayoutParams(new RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.MATCH_PARENT,
            RadioGroup.LayoutParams.WRAP_CONTENT));
        rb.setText(candidate.getName());
        rb.setId(View.generateViewId());
        rb.setPadding(32, 32, 32, 32);
        group.addView(rb);
    }

    private void addCheckBox(LinearLayout group, Candidate candidate) {
        CheckBox cb = new CheckBox(requireContext());
        cb.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        cb.setText(candidate.getName());
        cb.setPadding(32, 32, 32, 32);
        group.addView(cb);
    }

    private void validateAndSubmitVote() {
        if (!validatePresident()) {
            showError("Please select a president");
            return;
        }

        if (!validateVicePresident()) {
            showError("Please select a vice president");
            return;
        }

        int senatorCount = validateSenators();
        if (senatorCount > 12) {
            showError("You can only select up to 12 senators");
            return;
        }
        if (senatorCount < 1) {
            showError("Please select at least 1 senator");
            return;
        }

        // If all validations pass, submit the vote
        submitVote();
    }

    private boolean validatePresident() {
        return presidentGroup.getCheckedRadioButtonId() != -1;
    }

    private boolean validateVicePresident() {
        return vpGroup.getCheckedRadioButtonId() != -1;
    }

    private int validateSenators() {
        int count = 0;
        for (int i = 0; i < senatorsGroup.getChildCount(); i++) {
            View child = senatorsGroup.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                count++;
            }
        }
        return count;
    }

    private void showError(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.buksu_deep_purple))
                .setTextColor(getResources().getColor(R.color.white))
                .show();
    }

    private void submitVote() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        
        // Get selected candidates
        RadioButton selectedPresident = presidentGroup.findViewById(presidentGroup.getCheckedRadioButtonId());
        RadioButton selectedVP = vpGroup.findViewById(vpGroup.getCheckedRadioButtonId());
        List<String> selectedSenators = new ArrayList<>();
        
        for (int i = 0; i < senatorsGroup.getChildCount(); i++) {
            View child = senatorsGroup.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                selectedSenators.add(((CheckBox) child).getText().toString());
            }
        }

        // Create vote document
        Map<String, Object> vote = new HashMap<>();
        vote.put("voterId", userId);
        vote.put("president", selectedPresident.getText().toString());
        vote.put("vicePresident", selectedVP.getText().toString());
        vote.put("senators", selectedSenators);
        vote.put("timestamp", new Date());

        // Submit vote and update voter status
        db.collection("votes").add(vote)
            .addOnSuccessListener(documentReference -> {
                // Update voter status
                db.collection("users").document(userId)
                    .update("hasVoted", true)
                    .addOnSuccessListener(aVoid -> {
                        // Update candidate vote counts
                        incrementCandidateVotes(selectedPresident.getText().toString());
                        incrementCandidateVotes(selectedVP.getText().toString());
                        for (String senator : selectedSenators) {
                            incrementCandidateVotes(senator);
                        }
                        showSuccess("Vote submitted successfully!");
                        clearSelections();
                    })
                    .addOnFailureListener(e -> showError("Error updating voter status: " + e.getMessage()));
            })
            .addOnFailureListener(e -> showError("Error submitting vote: " + e.getMessage()));
    }

    private void incrementCandidateVotes(String candidateName) {
        db.collection("candidates")
            .whereEqualTo("name", candidateName)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().update("voteCount", FieldValue.increment(1));
                }
            });
    }

    private void clearSelections() {
        // Clear selections
        presidentGroup.clearCheck();
        vpGroup.clearCheck();
        clearSenatorSelections();
    }

    private void clearSenatorSelections() {
        for (int i = 0; i < senatorsGroup.getChildCount(); i++) {
            View child = senatorsGroup.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(false);
            }
        }
    }

    private void showSuccess(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.buksu_deep_purple))
                .setTextColor(getResources().getColor(R.color.buksu_gold))
                .show();
    }
}
