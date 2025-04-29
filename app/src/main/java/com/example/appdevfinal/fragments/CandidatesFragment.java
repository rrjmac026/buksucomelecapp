package com.example.appdevfinal.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.example.appdevfinal.adapters.CandidateAdapter;
import com.example.appdevfinal.models.Candidate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CandidatesFragment extends Fragment implements CandidateAdapter.OnCandidateClickListener {
    private RecyclerView recyclerView;
    private CandidateAdapter adapter;
    private List<Candidate> candidates;
    private List<Candidate> allCandidates;
    private FirebaseFirestore db;
    private TextInputEditText searchView;
    private Spinner spinnerSort;
    private ListenerRegistration candidatesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_candidates_list, container, false);
        
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewCandidates);
        searchView = view.findViewById(R.id.searchView);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        
        allCandidates = new ArrayList<>();
        candidates = new ArrayList<>();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CandidateAdapter(candidates, this);
        recyclerView.setAdapter(adapter);

        setupSearchView();
        setupSortSpinner();
        attachCandidatesListener();

        FloatingActionButton fab = view.findViewById(R.id.fabAddCandidate);
        fab.setOnClickListener(v -> showAddDialog());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        attachCandidatesListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (candidatesListener != null) {
            candidatesListener.remove();
        }
    }

    private void attachCandidatesListener() {
        candidatesListener = db.collection("candidates")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    return;
                }

                candidates.clear();
                allCandidates.clear();
                
                for (QueryDocumentSnapshot doc : snapshots) {
                    Candidate candidate = doc.toObject(Candidate.class);
                    if (candidate != null) {
                        candidate.setId(doc.getId());
                        candidates.add(candidate);
                        allCandidates.add(candidate);
                    }
                }
                
                adapter.notifyDataSetChanged();
            });
    }

    private void setupSearchView() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCandidates(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Name (A-Z)", "Name (Z-A)", "Position", "Party List"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortCandidates(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterCandidates(String query) {
        if (allCandidates == null) return;
        
        List<Candidate> filteredList = new ArrayList<>();
        for (Candidate candidate : allCandidates) {
            if (candidate.getName() != null && candidate.getPosition() != null && candidate.getPartyList() != null) {
                if (candidate.getName().toLowerCase().contains(query.toLowerCase()) ||
                    candidate.getPosition().toLowerCase().contains(query.toLowerCase()) ||
                    candidate.getPartyList().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(candidate);
                }
            }
        }
        candidates.clear();
        candidates.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    private void sortCandidates(int sortType) {
        switch (sortType) {
            case 0: // Name (A-Z)
                candidates.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
                break;
            case 1: // Name (Z-A)
                candidates.sort((c1, c2) -> c2.getName().compareTo(c1.getName()));
                break;
            case 2: // Position
                candidates.sort((c1, c2) -> c1.getPosition().compareTo(c2.getPosition()));
                break;
            case 3: // Party List
                candidates.sort((c1, c2) -> c1.getPartyList().compareTo(c2.getPartyList()));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_candidate, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editPosition = dialogView.findViewById(R.id.editPosition);
        EditText editPartyList = dialogView.findViewById(R.id.editPartyList);
        EditText editPlatform = dialogView.findViewById(R.id.editPlatform);

        new AlertDialog.Builder(getContext())
            .setTitle("Add New Candidate")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                Candidate candidate = new Candidate(
                    editName.getText().toString(),
                    editPosition.getText().toString(),
                    editPartyList.getText().toString(),
                    editPlatform.getText().toString()
                );
                addCandidate(candidate);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addCandidate(Candidate candidate) {
        db.collection("candidates")
            .add(candidate)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Candidate added successfully", Toast.LENGTH_SHORT).show();
                // No need to reload - listener will handle it
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Error adding candidate", Toast.LENGTH_SHORT).show());
    }

    private void updateCandidate(Candidate candidate) {
        db.collection("candidates").document(candidate.getId())
            .set(candidate)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Candidate updated successfully", Toast.LENGTH_SHORT).show();
                // No need to reload - listener will handle it
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Error updating candidate", Toast.LENGTH_SHORT).show());
    }

    private void deleteCandidate(Candidate candidate) {
        db.collection("candidates").document(candidate.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Candidate deleted successfully", Toast.LENGTH_SHORT).show();
                // No need to reload - listener will handle it
            })
            .addOnFailureListener(e -> 
                Toast.makeText(getContext(), "Error deleting candidate", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onCandidateEdit(Candidate candidate) {
        showEditDialog(candidate);
    }

    @Override
    public void onCandidateDelete(Candidate candidate) {
        deleteCandidate(candidate);
    }

    private void showEditDialog(Candidate candidate) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_candidate, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editPosition = dialogView.findViewById(R.id.editPosition);
        EditText editPartyList = dialogView.findViewById(R.id.editPartyList);
        EditText editPlatform = dialogView.findViewById(R.id.editPlatform);

        // Pre-fill the fields
        editName.setText(candidate.getName());
        editPosition.setText(candidate.getPosition());
        editPartyList.setText(candidate.getPartyList());
        editPlatform.setText(candidate.getPlatform());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Candidate")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    candidate.setName(editName.getText().toString());
                    candidate.setPosition(editPosition.getText().toString());
                    candidate.setPartyList(editPartyList.getText().toString());
                    candidate.setPlatform(editPlatform.getText().toString());
                    updateCandidate(candidate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
