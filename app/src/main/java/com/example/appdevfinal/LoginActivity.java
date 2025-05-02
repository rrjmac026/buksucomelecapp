package com.example.appdevfinal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.content.res.Configuration;
import com.example.appdevfinal.services.AuthService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import android.util.Base64;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etEmail, etPassword;
    private AuthService authService;
    private ProgressBar loadingIndicator;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginUser());
        findViewById(R.id.btnGoogleLogin).setOnClickListener(v -> signInWithGoogle());
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("is_dark_mode", false);
        int defaultMode = isDarkMode ? 
            AppCompatDelegate.MODE_NIGHT_YES : 
            AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(defaultMode);
    }

    private void signInWithGoogle() {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        
        // Clear any existing sign in state
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            try {
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } catch (Exception e) {
                Log.e(TAG, "Google Sign In failed", e);
                showError("Google Sign In failed: " + e.getMessage());
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", user.getEmail());
                        userData.put("name", user.getDisplayName());
                        userData.put("role", "voter");
                        userData.put("lastLogin", new Date());

                        db.collection("users").document(user.getUid())
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    loadingIndicator.setVisibility(View.GONE);
                                    redirectBasedOnRole("voter");
                                })
                                .addOnFailureListener(e -> {
                                    loadingIndicator.setVisibility(View.GONE);
                                    showError("Failed to update user data: " + e.getMessage());
                                });
                    }
                });
    }

    private void handleLoginSuccess(FirebaseUser user) {
        loadingIndicator.setVisibility(View.VISIBLE);
        authService.getCurrentUserRole()
            .addOnSuccessListener(result -> {
                Object isAdminObj = result.getClaims().get("admin");
                Boolean isAdmin = isAdminObj instanceof Boolean && (Boolean) isAdminObj;
                redirectBasedOnRole(isAdmin ? "admin" : "voter");
            })
            .addOnFailureListener(e -> {
                loadingIndicator.setVisibility(View.GONE);
                showError("Failed to get user role: " + e.getMessage());
            });
    }

    private void redirectBasedOnRole(String role) {
        Intent intent = new Intent(this,
            "admin".equals(role) ? AdminDashboardActivity.class : VoterDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void loginUser() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        loadingIndicator.setVisibility(View.GONE);
                        if (document.exists()) {
                            String role = document.getString("role");
                            Intent intent;
                            if ("admin".equals(role)) {
                                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, VoterDashboardActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                            auth.signOut();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error checking role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    });
            })
            .addOnFailureListener(e -> {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            boolean isDarkMode = 
                (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
            
            // Toggle theme
            int newMode = isDarkMode ? 
                AppCompatDelegate.MODE_NIGHT_NO : 
                AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(newMode);
            
            // Save preference
            getSharedPreferences("theme_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_dark_mode", !isDarkMode)
                .apply();
                
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
