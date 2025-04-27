package com.example.appdevfinal;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appdevfinal.services.AuthService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Base64;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailInput, passwordInput;
    private AuthService authService;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Check cached role first
        String cachedRole = authService.getCachedUserRole();
        if (cachedRole != null) {
            redirectBasedOnRole(cachedRole);
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Verify SHA-1 fingerprint
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(signature.toByteArray());
                String sha1 = bytesToHex(md.digest()).toUpperCase();
                Log.d(TAG, "SHA1: " + sha1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking signature", e);
        }

        // Configure Google Sign In with web client ID
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("283911684117-rolv87gsd5qeiavvalv8d29stjer8tv6.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup click listeners
        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.googleSignInButton).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.registerButton).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign In");
        // Clear any existing sign in state first
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
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
                .addOnSuccessListener(authResult -> handleLoginSuccess(authResult.getUser()))
                .addOnFailureListener(e -> Toast.makeText(this,
                    "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed directly with Firebase authentication
        proceedWithLogin(email, password);
    }

    private void proceedWithLogin(String email, String password) {
        loadingIndicator.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                
                // Check user role in Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        loadingIndicator.setVisibility(View.GONE);
                        if (document.exists()) {
                            String role = document.getString("role");
                            if ("admin".equals(role)) {
                                startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, VoterDashboardActivity.class));
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error checking role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
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
}
