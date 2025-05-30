package com.example.appdevfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.appdevfinal.fragments.FeedbackFragment;
import com.example.appdevfinal.fragments.RankingsFragment;
import com.example.appdevfinal.fragments.VoteFragment;
import com.example.appdevfinal.fragments.VoterDashboardFragment;
import com.example.appdevfinal.fragments.VoterProfileFragment;
import com.example.appdevfinal.fragments.VoterResultsFragment;
import com.example.appdevfinal.services.BibleVerseService;
import com.example.appdevfinal.services.BibleVerseService.BibleVerseCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class VoterDashboardActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {
    
    private DrawerLayout drawer;
    private View bibleVerseFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Voter Dashboard");
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new VoterDashboardFragment())
                .commit();
            navigationView.setCheckedItem(R.id.nav_voter_dashboard);
        }

        // Add Bible verse footer
        bibleVerseFooter = getLayoutInflater().inflate(R.layout.nav_footer_bible_verse, navigationView, false);
        navigationView.addView(bibleVerseFooter);
        loadDailyVerse();
    }

    private void loadDailyVerse() {
        TextView verseText = bibleVerseFooter.findViewById(R.id.bibleVerseText);
        TextView referenceText = bibleVerseFooter.findViewById(R.id.bibleVerseReference);

        BibleVerseService.startVerseUpdates(new BibleVerseService.BibleVerseCallback() {
            @Override
            public void onVerseReceived(String verse, String reference) {
                runOnUiThread(() -> {
                    verseText.setText(verse);
                    referenceText.setText(reference);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    verseText.setText("For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.");
                    referenceText.setText("John 3:16");
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BibleVerseService.stopVerseUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.voter_dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            int nightModeFlags = getResources().getConfiguration().uiMode & 
                               android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            // Save theme preference
            getSharedPreferences("theme_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_dark_mode", 
                    nightModeFlags != android.content.res.Configuration.UI_MODE_NIGHT_YES)
                .apply();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_voter_dashboard) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new VoterDashboardFragment())
                .commit();
        } else if (id == R.id.nav_vote) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new VoteFragment())
                .commit();
        } else if (id == R.id.nav_feedback) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new FeedbackFragment())
                .commit();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
