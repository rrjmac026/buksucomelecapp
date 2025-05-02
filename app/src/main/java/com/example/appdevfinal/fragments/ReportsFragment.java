package com.example.appdevfinal.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.R;
import com.example.appdevfinal.utils.ReportGenerator;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private FirebaseFirestore db;
    private MaterialCardView votingReportCard, feedbackReportCard;
    private ProgressBar progressBar;
    private boolean isGeneratingVotingReport = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        
        db = FirebaseFirestore.getInstance();
        votingReportCard = view.findViewById(R.id.votingReportCard);
        feedbackReportCard = view.findViewById(R.id.feedbackReportCard);
        progressBar = view.findViewById(R.id.progressBar);

        setupButtons(view);

        return view;
    }

    private void checkPermissionAndGenerate(boolean isVotingReport) {
        this.isGeneratingVotingReport = isVotingReport;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivityForResult(intent, STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            } else {
                proceedWithReport();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            } else {
                proceedWithReport();
            }
        }
    }

    private void proceedWithReport() {
        if (isGeneratingVotingReport) {
            generateVotingReport();
        } else {
            generateFeedbackReport();
        }
    }

    private void setupButtons(View view) {
        votingReportCard = view.findViewById(R.id.votingReportCard);
        feedbackReportCard = view.findViewById(R.id.feedbackReportCard);
        
        votingReportCard.setOnClickListener(v -> checkPermissionAndGenerate(true));
        feedbackReportCard.setOnClickListener(v -> checkPermissionAndGenerate(false));
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void generateVotingReport() {
        if (!isAdded()) return;
        
        isGeneratingVotingReport = true;
        showLoading(true);
        votingReportCard.setEnabled(false);
        feedbackReportCard.setEnabled(false);

        db.collection("votes")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isAdded()) return;
                
                List<Map<String, Object>> votingData = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    votingData.add(document.getData());
                }
                
                try {
                    ReportGenerator.generateVotingReport(requireContext(), votingData);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    showLoading(false);
                    votingReportCard.setEnabled(true);
                    feedbackReportCard.setEnabled(true);
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                showLoading(false);
                votingReportCard.setEnabled(true);
                feedbackReportCard.setEnabled(true);
                Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void generateFeedbackReport() {
        if (!isAdded()) return;
        
        isGeneratingVotingReport = false;
        showLoading(true);
        votingReportCard.setEnabled(false);
        feedbackReportCard.setEnabled(false);

        db.collection("feedback")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!isAdded()) return;
                
                List<Map<String, Object>> feedbackData = new ArrayList<>();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    feedbackData.add(document.getData());
                }
                
                try {
                    ReportGenerator.generateFeedbackReport(requireContext(), feedbackData);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    showLoading(false);
                    votingReportCard.setEnabled(true);
                    feedbackReportCard.setEnabled(true);
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                showLoading(false);
                votingReportCard.setEnabled(true);
                feedbackReportCard.setEnabled(true);
                Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    proceedWithReport();
                } else {
                    Toast.makeText(requireContext(), 
                        "Storage permission required to generate reports", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedWithReport();
            } else {
                Toast.makeText(requireContext(), 
                    "Storage permission required to generate reports", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}
