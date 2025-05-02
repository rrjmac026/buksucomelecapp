package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.util.Log;
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

        // Check network connectivity first
        if (!isNetworkAvailable()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_LONG).show();
            }
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        // First get candidates to establish the structure
        db.collection("candidates")
            .get()
            .addOnSuccessListener(candidatesSnapshot -> {
                List<RankingItem> presidents = new ArrayList<>();
                List<RankingItem> vps = new ArrayList<>();
                List<RankingItem> senators = new ArrayList<>();

                // Create initial ranking items with 0 votes
                for (var doc : candidatesSnapshot) {
                    String name = doc.getString("name");
                    String position = doc.getString("position");
                    String partyList = doc.getString("partyList");

                    if (name != null && position != null) {
                        RankingItem item = new RankingItem(name, position, partyList, 0);
                        switch (position.toLowerCase()) {
                            case "president": presidents.add(item); break;
                            case "vice president": vps.add(item); break;
                            case "senator": senators.add(item); break;
                        }
                    }
                }

                // Now count votes exactly like in Admin Dashboard
                db.collection("votes")
                    .get()
                    .addOnSuccessListener(votesSnapshot -> {
                        Log.d("RankingsFragment", "Total votes found: " + votesSnapshot.size());
                        
                        // Count votes
                        for (var voteDoc : votesSnapshot) {
                            // Count president votes
                            String president = voteDoc.getString("president");
                            if (president != null) {
                                president = president.trim().replaceAll("\\s+", " ");
                                for (RankingItem item : presidents) {
                                    String comparison = (item.name + " " + item.partyList).trim().replaceAll("\\s+", " ");
                                    if (president.equals(comparison)) {
                                        item.votes++;
                                        break;
                                    }
                                }
                            }

                            // Count VP votes
                            String vp = voteDoc.getString("vicePresident");
                            if (vp != null) {
                                vp = vp.trim().replaceAll("\\s+", " ");
                                for (RankingItem item : vps) {
                                    String comparison = (item.name + " " + item.partyList).trim().replaceAll("\\s+", " ");
                                    if (vp.equals(comparison)) {
                                        item.votes++;
                                        break;
                                    }
                                }
                            }

                            // Count senator votes
                            List<String> senatorVotes = (List<String>) voteDoc.get("senators");
                            if (senatorVotes != null) {
                                for (String senatorVote : senatorVotes) {
                                    String cleanSenatorVote = senatorVote.trim().replaceAll("\\s+", " ");
                                    for (RankingItem item : senators) {
                                        String comparison = (item.name + " " + item.partyList).trim().replaceAll("\\s+", " ");
                                        if (cleanSenatorVote.equals(comparison)) {
                                            item.votes++;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        // After counting all votes, log the results
                        Log.d("RankingsFragment", "Final vote counts:");
                        for (RankingItem item : presidents) {
                            Log.d("RankingsFragment", "President " + item.name + ": " + item.votes + " votes");
                        }
                        for (RankingItem item : vps) {
                            Log.d("RankingsFragment", "VP " + item.name + ": " + item.votes + " votes");
                        }
                        for (RankingItem item : senators) {
                            Log.d("RankingsFragment", "Senator " + item.name + ": " + item.votes + " votes");
                        }

                        // Sort by votes
                        Comparator<RankingItem> byVotes = (a, b) -> Integer.compare(b.votes, a.votes);
                        presidents.sort(byVotes);
                        vps.sort(byVotes);
                        senators.sort(byVotes);

                        // Create final list
                        List<RankingItem> allRankings = new ArrayList<>();
                        
                        if (!presidents.isEmpty()) {
                            allRankings.add(new RankingItem("PRESIDENT", "", "", -1));
                            allRankings.addAll(presidents);
                        }
                        if (!vps.isEmpty()) {
                            allRankings.add(new RankingItem("VICE PRESIDENT", "", "", -1));
                            allRankings.addAll(vps);
                        }
                        if (!senators.isEmpty()) {
                            allRankings.add(new RankingItem("SENATORS", "", "", -1));
                            allRankings.addAll(senators);
                        }

                        loadingProgressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(new RankingsAdapter(allRankings));
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load votes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        loadingProgressBar.setVisibility(View.GONE);
                    });
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load candidates: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                loadingProgressBar.setVisibility(View.GONE);
            });
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) 
            getContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
