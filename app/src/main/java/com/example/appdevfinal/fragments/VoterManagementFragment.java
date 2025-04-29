package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.User;
import com.example.appdevfinal.adapters.VoterAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class VoterManagementFragment extends Fragment {
    private RecyclerView voterRecyclerView;
    private FirebaseFirestore db;
    private VoterAdapter voterAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voter_management, container, false);
        
        db = FirebaseFirestore.getInstance();
        voterRecyclerView = view.findViewById(R.id.voterRecyclerView);
        
        // Initialize RecyclerView with decoration
        voterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        voterRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        voterAdapter = new VoterAdapter(new ArrayList<>());
        voterRecyclerView.setAdapter(voterAdapter);
        
        loadVoters();
        
        return view;
    }

    private void loadVoters() {
        ArrayList<User> users = new ArrayList<>();
        db.collection("users")
            .whereEqualTo("role", "voter")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        user.setId(document.getId());
                        users.add(user);
                    }
                }
                voterAdapter.updateVoters(users);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error loading voters: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}
