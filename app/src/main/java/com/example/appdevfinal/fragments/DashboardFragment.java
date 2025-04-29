package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {
    private TextView votingStatusText;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        votingStatusText = view.findViewById(R.id.votingStatusText);
        
        loadUserData();
        return view;
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    
                    // Only show voting status for voters, not admins
                    if ("voter".equals(role)) {
                        Boolean hasVoted = documentSnapshot.getBoolean("hasVoted");
                        if (hasVoted != null && hasVoted) {
                            votingStatusText.setText("Voting Status: You have already voted");
                        } else {
                            votingStatusText.setText("Voting Status: You haven't voted yet");
                        }
                        votingStatusText.setVisibility(View.VISIBLE);
                    } else {
                        // Hide voting status for admins
                        votingStatusText.setVisibility(View.GONE);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e("DashboardFragment", "Error loading user data", e));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // Reload data when fragment resumes
    }
}
