package com.example.doctorapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.UserModel;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivitySignupBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private SessionManager sessionManager;
    private MainViewModel viewModel;
    private List<LanguageModel> languageList = new ArrayList<>();
    private ArrayAdapter<String> languageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupSpinners();
        loadLanguages();
        setupListeners();
    }

    private void setupSpinners() {
        // Thiết lập Spinner giới tính
        List<String> genderOptions = Arrays.asList( "Male", "Female", "Other" );
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.signupGender.setAdapter(genderAdapter);

        // Thiết lập Spinner ngôn ngữ
        languageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.signupLanguage.setAdapter(languageAdapter);
    }

    private void loadLanguages() {
        viewModel.loadLanguages();
        viewModel.getLanguages().observe(this, languages -> {
            if (languages != null) {
                languageList = languages;
                List<String> languageNames = new ArrayList<>();
                for (LanguageModel lang : languages) {
                    languageNames.add(lang.getName());
                }
                languageAdapter.clear();
                languageAdapter.addAll(languageNames);
                languageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupListeners() {
        binding.signupButton.setOnClickListener(v -> registerUser());
        binding.loginRedirectText.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private boolean validateUserInput(String name, String email, String username, String password, String gender, String language) {
        if (name.isEmpty()) {
            binding.signupName.setError("Name cannot be empty");
            return false;
        }
        if (email.isEmpty()) {
            binding.signupEmail.setError("Email cannot be empty");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            binding.signupEmail.setError("Invalid email format");
            return false;
        }
        if (username.isEmpty()) {
            binding.signupUsername.setError("Username cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            binding.signupPassword.setError("Password cannot be empty");
            return false;
        }
        if (password.length() < 6) {
            binding.signupPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (language.isEmpty()) {
            Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser() {
        String name = binding.signupName.getText().toString().trim();
        String email = binding.signupEmail.getText().toString().trim();
        String username = binding.signupUsername.getText().toString().trim();
        String password = binding.signupPassword.getText().toString().trim();
        String gender = binding.signupGender.getSelectedItem() != null ?
                binding.signupGender.getSelectedItem().toString() : "";
        String language = binding.signupLanguage.getSelectedItem() != null ?
                getLanguageId(binding.signupLanguage.getSelectedItem().toString()) : "";

        if (!validateUserInput(name, email, username, password, gender, language)) {
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(
                Constants.DB_PATH_USERS);
        DatabaseReference idCounterRef = FirebaseDatabase.getInstance().getReference("idCounter");

        idCounterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int newId = snapshot.exists() ? snapshot.getValue(Integer.class) + 1 : 1;

                String userId = usersRef.push().getKey();
                UserModel userModel = new UserModel(newId, name, email, username, password, gender, language, "USER", true);

                usersRef.child(userId).setValue(userModel)
                        .addOnSuccessListener(aVoid -> {
                            sessionManager.saveUserSession(userId, newId, language);
                            Toast.makeText(SignupActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(SignupActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(SignupActivity.this, error, "Failed to register user");
            }
        });
    }

    private String getLanguageId(String languageName) {
        for(LanguageModel lang : languageList) {
            if(lang.getName().equals(languageName)) {
                return lang.getId();
            }
        }
        return "";
    }

}