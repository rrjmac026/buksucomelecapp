package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar loadingProgressBar;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rankings, container, false);
        
        recyclerView = view.findViewById(R.id.rankingsRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadRankings();
        
        return view;
    }

    private void loadRankings() {
        if (!isAdded()) return;
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        db.collection("votes").get().addOnSuccessListener(votesSnapshot -> {
            if (!isAdded()) return;

            Map<String, Integer> rankings = new HashMap<>();
            Map<String, String> candidatePositions = new HashMap<>();
            
            for (var vote : votesSnapshot.getDocuments()) {
                String president = vote.getString("president");
                if (president != null) {
                    rankings.put(president, rankings.getOrDefault(president, 0) + 1);
                    candidatePositions.put(president, "President");
                }
                
                String vp = vote.getString("vicePresident");
                if (vp != null) {
                    rankings.put(vp, rankings.getOrDefault(vp, 0) + 1);
                    candidatePositions.put(vp, "Vice President");
                }
                
                List<String> senators = (List<String>) vote.get("senators");
                if (senators != null) {
                    for (String senator : senators) {
                        rankings.put(senator, rankings.getOrDefault(senator, 0) + 1);
                        candidatePositions.put(senator, "Senator");
                    }
                }
            }

            db.collection("candidates").get().addOnSuccessListener(candidatesSnapshot -> {
                if (!isAdded()) return;
                
                List<RankingItem> presidents = new ArrayList<>();
                List<RankingItem> vicePresidents = new ArrayList<>();
                List<RankingItem> senators = new ArrayList<>();
                
                for (var doc : candidatesSnapshot.getDocuments()) {
                    String name = doc.getString("name");
                    String position = doc.getString("position");
                    String partyList = doc.getString("partyList");
                    
                    if (name != null && position != null && rankings.containsKey(name)) {
                        int votes = rankings.get(name);
                        RankingItem item = new RankingItem(name, position, partyList, votes);
                        
                        switch (position.toLowerCase()) {
                            case "president":
                                presidents.add(item);
                                break;
                            case "vice president":
                                vicePresidents.add(item);
                                break;
                            case "senator":
                                senators.add(item);
                                break;
                        }
                    }
                }
                
                // Sort each category by vote count
                Comparator<RankingItem> byVotes = (a, b) -> Integer.compare(b.votes, a.votes);
                presidents.sort(byVotes);
                vicePresidents.sort(byVotes);
                senators.sort(byVotes);
                
                // Combine all items maintaining position order
                List<RankingItem> allRankings = new ArrayList<>();
                
                // Add section headers and items
                if (!presidents.isEmpty()) {
                    allRankings.add(new RankingItem("PRESIDENT", "", "", -1)); // Header
                    allRankings.addAll(presidents);
                }
                
                if (!vicePresidents.isEmpty()) {
                    allRankings.add(new RankingItem("VICE PRESIDENT", "", "", -1)); // Header
                    allRankings.addAll(vicePresidents);
                }
                
                if (!senators.isEmpty()) {
                    allRankings.add(new RankingItem("SENATORS", "", "", -1)); // Header
                    allRankings.addAll(senators);
                }
                
                loadingProgressBar.setVisibility(View.GONE);
                recyclerView.setAdapter(new RankingsAdapter(allRankings));
            });
        });
    }
    
    private static class RankingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private final List<RankingItem> items;

        RankingsAdapter(List<RankingItem> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).votes == -1 ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_ranking_header, parent, false);
                return new HeaderViewHolder(view);
            }
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ranking, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RankingItem item = items.get(position);
            if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).headerText.setText(item.name);
            } else if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemHolder = (ItemViewHolder) holder;
                itemHolder.candidateNameText.setText(item.name);
                itemHolder.partyListText.setText(item.partyList);
                itemHolder.voteCountText.setText(item.votes + " votes");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView headerText;

            HeaderViewHolder(View view) {
                super(view);
                headerText = view.findViewById(R.id.headerText);
            }
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView candidateNameText;
            TextView partyListText;
            TextView voteCountText;

            ItemViewHolder(View view) {
                super(view);
                candidateNameText = view.findViewById(R.id.candidateNameText);
                partyListText = view.findViewById(R.id.partyListText);
                voteCountText = view.findViewById(R.id.voteCountText);
            }
        }
    }

    private static class RankingItem {
        String name;
        String position;
        String partyList;
        int votes;

        RankingItem(String name, String position, String partyList, int votes) {
            this.name = name;
            this.position = position;
            this.partyList = partyList;
            this.votes = votes;
        }
    }
}
