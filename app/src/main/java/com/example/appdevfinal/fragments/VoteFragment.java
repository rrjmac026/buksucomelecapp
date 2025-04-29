package com.example.appdevfinal.fragments;

import com.example.appdevfinal.adapters.CandidateAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteFragment extends Fragment {
    private RecyclerView candidatesRecyclerView;
    private CandidateAdapter adapter;
    private Button submitVoteButton;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);
        
        candidatesRecyclerView = view.findViewById(R.id.candidatesRecyclerView);
        submitVoteButton = view.findViewById(R.id.submitVoteButton);
        db = FirebaseFirestore.getInstance();

        candidatesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadCandidates();
        setupVoteButton();
        
        return view;
    }

    private void loadCandidates() {
        db.collection("candidates").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> candidates = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    candidates.add(doc.getData());
                }
                adapter = new CandidateAdapter(candidates, true);
                candidatesRecyclerView.setAdapter(adapter);
            });
    }

    private void setupVoteButton() {
        submitVoteButton.setOnClickListener(v -> {
            Map<String, Object> selectedCandidate = adapter.getSelectedCandidate();
            if (selectedCandidate == null) {
                Toast.makeText(getContext(), "Please select a candidate", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> vote = new HashMap<>();
            vote.put("candidateId", selectedCandidate.get("id"));
            vote.put("candidateName", selectedCandidate.get("name"));
            vote.put("timestamp", new Date().toString());

            db.collection("votes").document(userId).set(vote)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Vote submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back to dashboard
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new VoterDashboardFragment())
                        .commit();
                });
        });
    }
}
