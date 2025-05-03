package com.example.appdevfinal.services;

import android.os.AsyncTask;
import android.os.Handler;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class BibleVerseService {
    private static final String API_KEY = "cb97fce5d2383a42d9cb0e1f318de7e7";
    private static final String BASE_URL = "https://api.scripture.api.bible/v1/bibles";
    private static final String[] VERSES = {
        "JHN.3.16", // John 3:16
        "PSA.23.1", // Psalm 23:1
        "PRO.3.5",  // Proverbs 3:5
        "PHP.4.13", // Philippians 4:13
        "ROM.8.28"  // Romans 8:28
    };
    private static final int UPDATE_INTERVAL = 30000; // 30 seconds
    private static Handler handler;
    private static Runnable updateRunnable;

    public interface BibleVerseCallback {
        void onVerseReceived(String verse, String reference);
        void onError(String error);
    }

    public static void startVerseUpdates(BibleVerseCallback callback) {
        if (handler == null) {
            handler = new Handler();
        }
        
        if (updateRunnable == null) {
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    getDailyVerse(callback);
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            };
        }

        // Start updates
        handler.post(updateRunnable);
    }

    public static void stopVerseUpdates() {
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }

    public static void getDailyVerse(BibleVerseCallback callback) {
        String randomVerse = VERSES[new Random().nextInt(VERSES.length)];
        
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(BASE_URL + "/de4e12af7f28f599-02/verses/" + params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("api-key", API_KEY);

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if (result != null) {
                        JSONObject json = new JSONObject(result);
                        JSONObject data = json.getJSONObject("data");
                        String verse = data.getString("content").replaceAll("<[^>]*>", "");
                        String reference = data.getString("reference");
                        callback.onVerseReceived(verse, reference);
                    } else {
                        callback.onError("Error fetching verse");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        }.execute(randomVerse);
    }
}
