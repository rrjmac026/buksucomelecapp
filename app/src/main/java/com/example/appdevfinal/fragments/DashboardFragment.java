package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {
    private TextView candidateCount;
    private TextView voterCount;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        db = FirebaseFirestore.getInstance();
        candidateCount = view.findViewById(R.id.candidateCount);
        voterCount = view.findViewById(R.id.voterCount);

        loadStatistics();
        
        return view;
    }

    private void loadStatistics() {
        // Get candidate count
        db.collection("candidates").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                candidateCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });

        // Get voter count
        db.collection("voters").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                voterCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics(); // Refresh statistics when returning to dashboard
    }
}
