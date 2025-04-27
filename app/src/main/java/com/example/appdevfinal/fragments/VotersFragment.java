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
import com.example.appdevfinal.models.Voter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class VotersFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Voter> voters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voters, container, false);
        
        db = FirebaseFirestore.getInstance();
        voters = new ArrayList<>();
        
        recyclerView = view.findViewById(R.id.recyclerViewVoters);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadVoters();

        return view;
    }

    private void loadVoters() {
        if (db == null) return;

        db.collection("voters")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(getContext(), "Error loading voters: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    voters.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Voter voter = doc.toObject(Voter.class);
                        if (voter != null) {
                            voter.setId(doc.getId());
                            voters.add(voter);
                        }
                    }
                }
            });
    }
}
