package com.example.appdevfinal.helpers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.appdevfinal.models.Candidate;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private static final String CANDIDATES_COLLECTION = "candidates";

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> addCandidate(Candidate candidate) {
        Map<String, Object> candidateData = new HashMap<>();
        candidateData.put("name", candidate.getName());
        candidateData.put("position", candidate.getPosition());
        candidateData.put("partyList", candidate.getPartyList());
        candidateData.put("platform", candidate.getPlatform());
        
        return db.collection(CANDIDATES_COLLECTION).document().set(candidateData);
    }

    public Task<QuerySnapshot> getAllCandidates() {
        return db.collection(CANDIDATES_COLLECTION).get();
    }

    public Task<Void> updateCandidate(String id, Candidate candidate) {
        return db.collection(CANDIDATES_COLLECTION).document(id).update(
            "name", candidate.getName(),
            "position", candidate.getPosition(),
            "partyList", candidate.getPartyList(),
            "platform", candidate.getPlatform()
        );
    }

    public Task<Void> deleteCandidate(String id) {
        return db.collection(CANDIDATES_COLLECTION).document(id).delete();
    }
}
