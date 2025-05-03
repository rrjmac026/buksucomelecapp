package com.example.appdevfinal.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.appdevfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;  // Make this import explicit
import java.util.Map;   // Make this import explicit

public class ReportGenerator {
    public static final int STORAGE_PERMISSION_CODE = 101;

    // Add BukSU colors
    private static final BaseColor BUKSU_DEEP_PURPLE = new BaseColor(48, 37, 82); // #302552
    private static final BaseColor BUKSU_GOLD = new BaseColor(255, 198, 41);      // #FFC629

    private static class HeaderFooter extends PdfPageEventHelper {
        private final Context context;
        private PdfTemplate total;
        private BaseFont baseFont;
        private Image logo;

        HeaderFooter(Context context) {
            this.context = context;
            try {
                // Convert app icon to PDF Image
                Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.ic_icon)).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapData = stream.toByteArray();
                logo = Image.getInstance(bitmapData);
                logo.scaleToFit(50, 50);
                
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle rect = document.getPageSize();
            
            // Add purple header background
            cb.setColorFill(BUKSU_DEEP_PURPLE);
            cb.rectangle(0, rect.getHeight() - 80, rect.getWidth(), 80);
            cb.fill();
            
            // Header
            try {
                // Add logo
                logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                cb.addImage(logo);
                
                // Add header text in gold
                cb.beginText();
                cb.setFontAndSize(baseFont, 12);
                cb.setColorFill(BUKSU_GOLD);
                cb.showTextAligned(Element.ALIGN_CENTER, 
                    "BukSU_COMELEC2k25", 
                    document.getPageSize().getWidth() / 2, 
                    document.getPageSize().getHeight() - 30, 0);
                cb.endText();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add gold footer line
            cb.setColorStroke(BUKSU_GOLD);
            cb.setLineWidth(1.5f);
            cb.moveTo(30, 45);
            cb.lineTo(rect.getWidth() - 30, 45);
            cb.stroke();

            // Footer text in deep purple
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setColorFill(BUKSU_DEEP_PURPLE);
            cb.showTextAligned(Element.ALIGN_CENTER, 
                "Page " + writer.getPageNumber() + " of ", 
                document.getPageSize().getWidth() / 2 - 20, 30, 0);
            cb.endText();
            
            cb.addTemplate(total, document.getPageSize().getWidth() / 2 + 4, 30);
            
            // Add timestamp to footer
            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            cb.showTextAligned(Element.ALIGN_RIGHT, 
                "Generated on: " + timestamp, 
                document.getPageSize().getWidth() - 30, 30, 0);
            cb.endText();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(baseFont, 8);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        }
    }

    public static boolean checkPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void generateVotingReport(Context context, java.util.List<Map<String, Object>> votingData) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "Storage Permission Required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String fileName = "VotingReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            
            Document document = new Document(PageSize.A4, 36, 36, 80, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            writer.setPageEvent(new HeaderFooter(context));
            
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BUKSU_DEEP_PURPLE);
            Paragraph title = new Paragraph("Voting Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Style the table
            PdfPTable table = new PdfPTable(new float[]{2, 1, 2, 2, 3});
            table.setWidthPercentage(100);
            
            // Add table headers with BukSU colors
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BUKSU_GOLD);
            
            // Create and style header cells
            String[] headers = {"Voter Name", "ID", "President", "Vice President", "Senators"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BUKSU_DEEP_PURPLE);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Style content font with deep purple color
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BUKSU_DEEP_PURPLE);
            AtomicInteger processedVotes = new AtomicInteger(0);
            
            for (Map<String, Object> vote : votingData) {
                String voterId = (String) vote.get("voterId");
                
                db.collection("users").document(voterId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        try {
                            String voterName = userDoc.getString("name");
                            
                            table.addCell(new PdfPCell(new Phrase(voterName != null ? voterName : "Unknown", contentFont)));
                            table.addCell(new PdfPCell(new Phrase(voterId, contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("president")), contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("vicePresident")), contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("senators")), contentFont)));
                            
                            if (processedVotes.incrementAndGet() == votingData.size()) {
                                document.add(table);
                                document.close();
                                Toast.makeText(context, "Report saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                            }
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            table.addCell(new PdfPCell(new Phrase("Unknown", contentFont)));
                            table.addCell(new PdfPCell(new Phrase(voterId, contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("president")), contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("vicePresident")), contentFont)));
                            table.addCell(new PdfPCell(new Phrase(String.valueOf(vote.get("senators")), contentFont)));
                            
                            if (processedVotes.incrementAndGet() == votingData.size()) {
                                document.add(table);
                                document.close();
                                Toast.makeText(context, "Report saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                            }
                        } catch (DocumentException docException) {
                            docException.printStackTrace();
                        }
                    });
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error generating report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void generateFeedbackReport(Context context, java.util.List<Map<String, Object>> feedbackData) {
        if (!checkPermission(context)) {
            Toast.makeText(context, "Storage Permission Required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String fileName = "FeedbackReport_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            
            Document document = new Document(PageSize.A4, 36, 36, 80, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            writer.setPageEvent(new HeaderFooter(context));
            
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BUKSU_DEEP_PURPLE);
            Paragraph title = new Paragraph("Feedback Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Style the feedback content
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BUKSU_DEEP_PURPLE);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BUKSU_DEEP_PURPLE);
            
            AtomicInteger processedFeedback = new AtomicInteger(0);
            
            for (Map<String, Object> feedback : feedbackData) {
                String userId = (String) feedback.get("userId");
                
                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        try {
                            String userName = userDoc.getString("name");
                            
                            // Add styled sections
                            Paragraph section = new Paragraph();
                            section.add(new Phrase("User Feedback\n", headerFont));
                            section.add(new Phrase("Name: " + (userName != null ? userName : "Unknown") + "\n", contentFont));
                            section.add(new Phrase("User ID: " + userId + "\n", contentFont));
                            section.add(new Phrase("Feedback: " + feedback.get("feedback") + "\n", contentFont));
                            section.add(new Phrase("Rating: " + feedback.get("rating") + "\n", contentFont));
                            section.add(new Phrase("Timestamp: " + feedback.get("timestamp") + "\n\n", contentFont));
                            
                            // Add gold line between feedback entries
                            LineSeparator separator = new LineSeparator(1, 100, BUKSU_GOLD, Element.ALIGN_CENTER, -5);
                            section.add(separator);
                            
                            document.add(section);
                            
                            if (processedFeedback.incrementAndGet() == feedbackData.size()) {
                                document.close();
                                Toast.makeText(context, "Report saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                            }
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            // If we can't get the user name, still add the feedback with "Unknown" name
                            Paragraph section = new Paragraph();
                            section.add(new Phrase("User Feedback\n", headerFont));
                            section.add(new Phrase("Name: Unknown\n", contentFont));
                            section.add(new Phrase("User ID: " + userId + "\n", contentFont));
                            section.add(new Phrase("Feedback: " + feedback.get("feedback") + "\n", contentFont));
                            section.add(new Phrase("Rating: " + feedback.get("rating") + "\n", contentFont));
                            section.add(new Phrase("Timestamp: " + feedback.get("timestamp") + "\n\n", contentFont));
                            
                            LineSeparator separator = new LineSeparator(1, 100, BUKSU_GOLD, Element.ALIGN_CENTER, -5);
                            section.add(separator);
                            
                            document.add(section);
                            
                            if (processedFeedback.incrementAndGet() == feedbackData.size()) {
                                document.close();
                                Toast.makeText(context, "Report saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                            }
                        } catch (DocumentException docException) {
                            docException.printStackTrace();
                        }
                    });
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error generating report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
