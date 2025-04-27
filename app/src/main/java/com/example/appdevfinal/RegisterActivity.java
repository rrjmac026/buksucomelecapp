package com.example.appdevfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailInput, passwordInput, confirmPasswordInput, nameInput, studentNumberInput;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        nameInput = findViewById(R.id.nameInput);
        studentNumberInput = findViewById(R.id.studentNumberInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        findViewById(R.id.registerButton).setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String studentNumber = studentNumberInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || studentNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                Log.d(TAG, "Created auth user: " + userId);

                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("name", nameInput.getText().toString());
                user.put("studentNumber", studentNumberInput.getText().toString());
                user.put("role", "voter");
                user.put("createdAt", new Date());

                // Store in "users" collection
                db.collection("users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "User profile created with ID: " + userId);
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding user", e);
                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating user", e);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
