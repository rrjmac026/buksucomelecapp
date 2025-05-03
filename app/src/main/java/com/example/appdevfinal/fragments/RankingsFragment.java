package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.example.appdevfinal.models.Candidate;  // Add this import
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsFragment extends Fragment {
    private FirebaseFirestore db;
    private Map<String, Integer> voteCounts = new HashMap<>();
    private Map<String, Candidate> candidatesMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rankings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        fetchVotesAndUpdateRankings();
    }

    private void fetchVotesAndUpdateRankings() {
        // First get all candidates
        db.collection("candidates").get()
            .addOnSuccessListener(candidateSnapshots -> {
                candidatesMap.clear();
                voteCounts.clear();

                // Store candidates and initialize vote counts
                for (DocumentSnapshot doc : candidateSnapshots) {
                    Candidate candidate = doc.toObject(Candidate.class);
                    if (candidate != null) {
                        candidatesMap.put(candidate.getName(), candidate);
                        voteCounts.put(candidate.getName(), 0);
                    }
                }

                // Then fetch all votes
                db.collection("votes").get()
                    .addOnSuccessListener(voteSnapshots -> {
                        // Count votes for each position
                        for (DocumentSnapshot doc : voteSnapshots) {
                            countVote(doc.getString("president"));
                            countVote(doc.getString("vicePresident"));
                            List<String> senators = (List<String>) doc.get("senators");
                            if (senators != null) {
                                for (String senator : senators) {
                                    countVote(senator);
                                }
                            }
                        }
                        updateRankingsUI();
                    });
            });
    }

    private void countVote(String candidateName) {
        if (candidateName != null && voteCounts.containsKey(candidateName)) {
            voteCounts.put(candidateName, voteCounts.get(candidateName) + 1);
        }
    }

    private void updateRankingsUI() {
        // Get rankings for each position
        Map<String, List<CandidateRanking>> rankingsByPosition = new HashMap<>();

        for (Map.Entry<String, Candidate> entry : candidatesMap.entrySet()) {
            String name = entry.getKey();
            Candidate candidate = entry.getValue();
            int votes = voteCounts.getOrDefault(name, 0);

            String position = candidate.getPosition().toLowerCase();
            rankingsByPosition.computeIfAbsent(position, k -> new ArrayList<>())
                .add(new CandidateRanking(candidate, votes));
        }

        // Sort and display each position's rankings
        displayPositionRankings("president", rankingsByPosition.getOrDefault("president", new ArrayList<>()));
        displayPositionRankings("vice president", rankingsByPosition.getOrDefault("vice president", new ArrayList<>()));
        displayPositionRankings("senator", rankingsByPosition.getOrDefault("senator", new ArrayList<>()));
    }

    private void displayPositionRankings(String position, List<CandidateRanking> rankings) {
        // Sort by vote count in descending order
        rankings.sort((a, b) -> b.votes - a.votes);

        LinearLayout container = getPositionContainer(position);
        if (container != null) {
            container.removeAllViews();

            for (CandidateRanking ranking : rankings) {
                View rankingView = createRankingView(ranking);
                container.addView(rankingView);
            }
        }
    }

    private LinearLayout getPositionContainer(String position) {
        if (getView() == null) return null;

        int containerId;
        switch (position) {
            case "president":
                containerId = R.id.presidentRankingsContainer;
                break;
            case "vice president":
                containerId = R.id.vicePresidentRankingsContainer;
                break;
            case "senator":
                containerId = R.id.senatorRankingsContainer;
                break;
            default:
                return null;
        }
        return getView().findViewById(containerId);
    }

    private View createRankingView(CandidateRanking ranking) {
        View view = getLayoutInflater().inflate(R.layout.item_ranking, null);
        TextView nameText = view.findViewById(R.id.candidateNameText);
        TextView votesText = view.findViewById(R.id.votesText);
        TextView partyText = view.findViewById(R.id.partyText);

        nameText.setText(ranking.candidate.getName());
        votesText.setText(ranking.votes + " votes");
        partyText.setText(ranking.candidate.getPartyList());

        return view;
    }

    private static class CandidateRanking {
        Candidate candidate;
        int votes;

        CandidateRanking(Candidate candidate, int votes) {
            this.candidate = candidate;
            this.votes = votes;
        }
    }
}
