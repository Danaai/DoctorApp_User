package com.example.doctorapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.databinding.ActivityLoginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.loginButton.setOnClickListener(v -> {
            if (!validateLoginInput()) return;
            checkUserCredentials();
        });

        binding.signupRedirectText.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private boolean validateLoginInput() {
        String username = binding.loginUsername.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if (username.isEmpty()) {
            binding.loginUsername.setError("Username cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            binding.loginPassword.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    private void checkUserCredentials() {
        String username = binding.loginUsername.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_USERS);
        Query query = ref.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        Boolean isActive = userSnapshot.child("isActive").getValue(Boolean.class);
                        if (dbPassword != null && dbPassword.equals(password)) {
                            if (isActive != null && !isActive) {
                                binding.loginUsername.setError("Account is locked");
                                Toast.makeText(LoginActivity.this, "Your account is locked", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String uid = userSnapshot.getKey();
                            Integer userId = userSnapshot.child("id").getValue(Integer.class);
                            String language = userSnapshot.child("language").getValue(String.class);

                            sessionManager.saveUserSession(uid, userId != null ? userId : -1,
                                    language != null ? language : "en");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            return;
                        }
                        binding.loginPassword.setError("Invalid credentials");
                        binding.loginPassword.requestFocus();
                    }
                } else {
                    binding.loginUsername.setError("User not found");
                    binding.loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(LoginActivity.this, error, "Failed to authenticate user");
            }
        });
    }
}