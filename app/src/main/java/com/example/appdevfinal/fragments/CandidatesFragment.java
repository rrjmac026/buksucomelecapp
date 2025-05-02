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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                candidates.sort((c1, c2) -> {
                    // Custom position order: President -> Vice President -> Senator
                    int pos1 = getPositionWeight(c1.getPosition());
                    int pos2 = getPositionWeight(c2.getPosition());
                    return Integer.compare(pos1, pos2);
                });
                break;
            case 3: // Party List
                candidates.sort((c1, c2) -> c1.getPartyList().compareTo(c2.getPartyList()));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private int getPositionWeight(String position) {
        switch (position.toLowerCase()) {
            case "president": return 1;
            case "vice president": return 2;
            case "senator": return 3;
            default: return 4;
        }
    }

    private void setupPositionDropdown(MaterialAutoCompleteTextView positionDropdown) {
        String[] positions = new String[]{"President", "Vice President", "Senator"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            R.layout.item_dropdown_position, positions);
        positionDropdown.setAdapter(adapter);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_candidate, null);

        EditText nameInput = view.findViewById(R.id.editName);
        MaterialAutoCompleteTextView positionDropdown = view.findViewById(R.id.editPosition);
        EditText partyListInput = view.findViewById(R.id.editPartyList);
        EditText platformInput = view.findViewById(R.id.editPlatform);
        setupPositionDropdown(positionDropdown);

        builder.setView(view)
            .setTitle("Add New Candidate")
            .setPositiveButton("Add", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String position = positionDropdown.getText().toString();
                String partyList = partyListInput.getText().toString().trim();
                String platform = platformInput.getText().toString().trim();

                if (validateInput(name, position, partyList, platform)) {
                    Map<String, Object> candidateData = new HashMap<>();
                    candidateData.put("name", name);
                    candidateData.put("position", position);
                    candidateData.put("partyList", partyList);
                    candidateData.put("platform", platform);

                    db.collection("candidates")
                        .add(candidateData)
                        .addOnSuccessListener(documentReference -> {
                            showSuccess("Candidate added successfully");
                            loadCandidates();
                        })
                        .addOnFailureListener(e -> showError("Failed to add candidate: " + e.getMessage()));
                }
            })
            .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private boolean validateInput(String name, String position, String partyList, String platform) {
        if (name.isEmpty() || position.isEmpty() || partyList.isEmpty() || platform.isEmpty()) {
            showError("All fields must be filled");
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCandidateClick(Candidate candidate) {
        showCandidateDetails(candidate);
    }

    @Override
    public void onEditClick(Candidate candidate) {
        showEditDialog(candidate);
    }

    @Override
    public void onDeleteClick(Candidate candidate) {
        showDeleteConfirmation(candidate);
    }

    private void showEditDialog(Candidate candidate) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_candidate, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        MaterialAutoCompleteTextView positionDropdown = dialogView.findViewById(R.id.editPosition);
        EditText editPartyList = dialogView.findViewById(R.id.editPartyList);
        EditText editPlatform = dialogView.findViewById(R.id.editPlatform);
        setupPositionDropdown(positionDropdown);
        positionDropdown.setText(candidate.getPosition(), false);

        // Pre-fill the fields
        editName.setText(candidate.getName());
        editPartyList.setText(candidate.getPartyList());
        editPlatform.setText(candidate.getPlatform());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Candidate")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    candidate.setName(editName.getText().toString());
                    candidate.setPosition(positionDropdown.getText().toString());
                    candidate.setPartyList(editPartyList.getText().toString());
                    candidate.setPlatform(editPlatform.getText().toString());
                    updateCandidate(candidate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadCandidates() {
        db.collection("candidates")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                candidates.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Candidate candidate = doc.toObject(Candidate.class);
                    if (candidate != null) {
                        candidate.setId(doc.getId());
                        candidates.add(candidate);
                    }
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> showError("Error loading candidates: " + e.getMessage()));
    }

    private void showCandidateDetails(Candidate candidate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Candidate Details")
               .setMessage("Name: " + candidate.getName() + "\n" +
                         "Position: " + candidate.getPosition() + "\n" +
                         "Party List: " + candidate.getPartyList() + "\n" +
                         "Platform: " + candidate.getPlatform())
               .setPositiveButton("OK", null)
               .show();
    }

    private void showDeleteConfirmation(Candidate candidate) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Candidate")
            .setMessage("Are you sure you want to delete this candidate?")
            .setPositiveButton("Delete", (dialog, which) -> {
                db.collection("candidates").document(candidate.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        showSuccess("Candidate deleted successfully");
                        loadCandidates();
                    })
                    .addOnFailureListener(e -> showError("Failed to delete: " + e.getMessage()));
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateCandidate(Candidate candidate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", candidate.getName());
        updates.put("position", candidate.getPosition());
        updates.put("partyList", candidate.getPartyList());
        updates.put("platform", candidate.getPlatform());

        db.collection("candidates").document(candidate.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                showSuccess("Candidate updated successfully");
                loadCandidates();
            })
            .addOnFailureListener(e -> showError("Failed to update: " + e.getMessage()));
    }
}
