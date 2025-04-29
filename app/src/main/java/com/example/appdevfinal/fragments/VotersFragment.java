package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.adapters.VoterAdapter;
import com.example.appdevfinal.models.Voter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class VotersFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Voter> voters;
    private VoterAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voters, container, false);
        
        recyclerView = view.findViewById(R.id.votersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        db = FirebaseFirestore.getInstance();
        voters = new ArrayList<>();
        adapter = new VoterAdapter(voters);
        recyclerView.setAdapter(adapter);

        loadVoters();
        
        return view;
    }

    private void loadVoters() {
        db.collection("users")
            .whereEqualTo("role", "voter")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                voters.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Voter voter = document.toObject(Voter.class);
                    if (voter != null) {
                        voter.setId(document.getId());
                        voters.add(voter);
                    }
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error loading voters: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}
