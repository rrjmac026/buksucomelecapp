package com.example.appdevfinal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminReportsFragment extends Fragment {
    private FirebaseFirestore db;
    private TextView votingStatisticsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_reports, container, false);
        
        db = FirebaseFirestore.getInstance();
        votingStatisticsText = view.findViewById(R.id.votingStatisticsText);
        Button generateReportButton = view.findViewById(R.id.generateReportButton);
        
        generateReportButton.setOnClickListener(v -> generateReport());
        
        return view;
    }

    private void generateReport() {
        // Get total votes
        db.collection("votes").get().addOnSuccessListener(voteSnapshots -> {
            int totalVotes = voteSnapshots.size();
            
            // Get total voters
            db.collection("users").whereEqualTo("role", "voter").get()
                .addOnSuccessListener(voterSnapshots -> {
                    int totalVoters = voterSnapshots.size();
                    double turnoutPercentage = (totalVoters > 0) ? 
                        ((double) totalVotes / totalVoters) * 100 : 0;

                    String report = String.format(Locale.getDefault(),
                        "Voting Statistics Report\n\n" +
                        "Generated on: %s\n\n" +
                        "Total Registered Voters: %d\n" +
                        "Total Votes Cast: %d\n" +
                        "Voter Turnout: %.1f%%\n",
                        new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(new Date()),
                        totalVoters,
                        totalVotes,
                        turnoutPercentage);

                    votingStatisticsText.setText(report);
                });
        });
    }
}
