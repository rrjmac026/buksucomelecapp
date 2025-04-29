package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.User;
import com.example.appdevfinal.adapters.VoterAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
        
        loadRegisteredVoters();
        
        return view;
    }

    private void loadRegisteredVoters() {
        db.collection("users")
            .whereEqualTo("role", "voter") // Only get users with role "voter"
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    return;
                }
                ArrayList<User> users = new ArrayList<>();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        user.setUid(doc.getId());
                        users.add(user);
                    }
                    voterAdapter.updateVoters(users);
                }
            });
    }
}
