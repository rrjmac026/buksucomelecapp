package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.RatingBar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FeedbackFragment extends Fragment {
    private TextInputEditText feedbackInput;
    private RatingBar ratingBar;
    private MaterialButton submitButton;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        
        feedbackInput = view.findViewById(R.id.feedbackInput);
        ratingBar = view.findViewById(R.id.ratingBar);
        submitButton = view.findViewById(R.id.submitButton);
        db = FirebaseFirestore.getInstance();

        submitButton.setOnClickListener(v -> submitFeedback());
        
        return view;
    }

    private void submitFeedback() {
        String feedback = feedbackInput.getText().toString();
        float rating = ratingBar.getRating();

        if (feedback.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        feedbackData.put("feedback", feedback);
        feedbackData.put("rating", rating);
        feedbackData.put("timestamp", new Date());

        db.collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                feedbackInput.setText("");
                ratingBar.setRating(5);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Failed to submit feedback: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }
}
