package com.example.appdevfinal.services;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class AuthService {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER_ROLE = "user_role";
    private final FirebaseAuth auth;
    private final SharedPreferences prefs;
    private static final String ADMIN_EMAIL = "admin@buksu.edu.ph";

    public AuthService(Context context) {
        auth = FirebaseAuth.getInstance();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Task<GetTokenResult> getCurrentUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getIdToken(true)
            .addOnSuccessListener(result -> {
                String role = (Boolean) result.getClaims().get("admin") ? "admin" : "voter";
                prefs.edit().putString(KEY_USER_ROLE + "_" + user.getUid(), role).apply();
            });
    }

    public String getCachedUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? 
            prefs.getString(KEY_USER_ROLE + "_" + user.getUid(), null) : null;
    }

    public void signOut() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            prefs.edit().remove(KEY_USER_ROLE + "_" + user.getUid()).apply();
        }
        auth.signOut();
    }
}
