package com.example.appdevfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.appdevfinal.fragments.AdminDashboardFragment;
import com.example.appdevfinal.fragments.CandidatesFragment;
import com.example.appdevfinal.fragments.RankingsFragment;
import com.example.appdevfinal.fragments.SettingsFragment;
import com.example.appdevfinal.fragments.VoterManagementFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    
    // Fragment tags
    private static final String FRAGMENT_TAG_DASHBOARD = "dashboard";
    private static final String FRAGMENT_TAG_CANDIDATES = "candidates";
    private static final String FRAGMENT_TAG_SETTINGS = "settings";
    private static final String FRAGMENT_TAG_VOTERS = "voters";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminDashboardFragment())
                .commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AdminDashboardFragment())
                .commit();
        } else if (id == R.id.nav_candidates) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CandidatesFragment())
                .commit();
        } else if (id == R.id.nav_rankings) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RankingsFragment())
                .commit();
        } else if (id == R.id.nav_voter_management) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new VoterManagementFragment())
                .commit();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
