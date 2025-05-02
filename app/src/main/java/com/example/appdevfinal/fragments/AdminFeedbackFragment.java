package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RatingBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appdevfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminFeedbackFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FeedbackAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_feedback, container, false);
        
        recyclerView = view.findViewById(R.id.feedbackRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedbackAdapter();
        recyclerView.setAdapter(adapter);
        
        db = FirebaseFirestore.getInstance();
        loadFeedback();
        
        return view;
    }

    private void loadFeedback() {
        db.collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    return;
                }

                List<FeedbackItem> feedbackList = new ArrayList<>();
                for (var doc : snapshots) {
                    String userId = doc.getString("userId");
                    // Get user name for each feedback
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener(userDoc -> {
                            String userName = userDoc.getString("name");
                            FeedbackItem item = new FeedbackItem(
                                userName,
                                doc.getString("feedback"),
                                doc.getDouble("rating").floatValue(),
                                doc.getTimestamp("timestamp").toDate()
                            );
                            feedbackList.add(item);
                            adapter.updateFeedback(feedbackList);
                        });
                }
            });
    }

    private static class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
        private List<FeedbackItem> feedback = new ArrayList<>();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FeedbackItem item = feedback.get(position);
            holder.voterNameText.setText(item.voterName);
            holder.feedbackText.setText(item.feedback);
            holder.ratingBar.setRating(item.rating);
            holder.timestampText.setText(dateFormat.format(item.timestamp));
        }

        @Override
        public int getItemCount() {
            return feedback.size();
        }

        void updateFeedback(List<FeedbackItem> newFeedback) {
            feedback = new ArrayList<>(newFeedback);
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView voterNameText, feedbackText, timestampText;
            RatingBar ratingBar;

            ViewHolder(View view) {
                super(view);
                voterNameText = view.findViewById(R.id.voterNameText);
                feedbackText = view.findViewById(R.id.feedbackText);
                ratingBar = view.findViewById(R.id.ratingBar);
                timestampText = view.findViewById(R.id.timestampText);
            }
        }
    }

    private static class FeedbackItem {
        String voterName;
        String feedback;
        float rating;
        Date timestamp;

        FeedbackItem(String voterName, String feedback, float rating, Date timestamp) {
            this.voterName = voterName;
            this.feedback = feedback;
            this.rating = rating;
            this.timestamp = timestamp;
        }
    }
}
