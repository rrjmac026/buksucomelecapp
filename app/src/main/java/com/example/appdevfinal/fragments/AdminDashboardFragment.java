package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.Date;

public class AdminDashboardFragment extends Fragment {
    private TextView votesCountText, votersCountText, recentActivityText;
    private MaterialCardView viewResultsCard;
    private FirebaseFirestore db;
    private ListenerRegistration votesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        initializeViews(view);
        loadCounts();
        setupRealtimeUpdates();
        setupClickListeners();
        return view;
    }

    private void initializeViews(View view) {
        votesCountText = view.findViewById(R.id.votesCountText);
        votersCountText = view.findViewById(R.id.votersCountText);
        recentActivityText = view.findViewById(R.id.recentActivityText);
        viewResultsCard = view.findViewById(R.id.viewResultsCard);
        db = FirebaseFirestore.getInstance();
    }

    private void loadCounts() {
        // Get total voters count
        db.collection("users")
            .whereEqualTo("role", "voter")
            .get()
            .addOnSuccessListener(querySnapshot -> 
                votersCountText.setText(String.valueOf(querySnapshot.size())));

        // Get votes count
        db.collection("votes").get()
            .addOnSuccessListener(querySnapshot -> 
                votesCountText.setText(String.valueOf(querySnapshot.size())));
    }

    private void setupRealtimeUpdates() {
        votesListener = db.collection("votes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null || snapshots.isEmpty()) {
                    return;
                }

                DocumentSnapshot latestVote = snapshots.getDocuments().get(0);
                String voterId = latestVote.getString("voterId");
                
                // Get voter name
                db.collection("users").document(voterId).get()
                    .addOnSuccessListener(userDoc -> {
                        String voterName = userDoc.getString("name");
                        Timestamp timestamp = latestVote.getTimestamp("timestamp");
                        if (timestamp != null) {
                            String timeAgo = getTimeAgo(timestamp.toDate());
                            recentActivityText.setText("Latest Vote: " + voterName + "\n" + timeAgo);
                        }
                    });
            });
    }

    private void setupClickListeners() {
        if (viewResultsCard != null) {
            viewResultsCard.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RankingsFragment())
                    .addToBackStack(null)
                    .commit();
            });
        }
    }

    private String getTimeAgo(Date date) {
        if (date == null) return "";
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        long diffInSeconds = diffInMillis / 1000;
        
        if (diffInSeconds < 60) return "Just now";
        if (diffInSeconds < 3600) return (diffInSeconds / 60) + " minutes ago";
        if (diffInSeconds < 86400) return (diffInSeconds / 3600) + " hours ago";
        return (diffInSeconds / 86400) + " days ago";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (votesListener != null) {
            votesListener.remove();
        }
    }
}
