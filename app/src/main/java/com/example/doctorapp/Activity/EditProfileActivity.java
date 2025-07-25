package com.example.doctorapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;

import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.Domain.UserModel;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityEditProfileBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends BaseActivity {
    private ActivityEditProfileBinding binding;
    private SessionManager sessionManager;
    private MainViewModel viewModel;
    private boolean isPasswordVisible = false;
    private UserModel currentUser;

    private List<LanguageModel> languageList = new ArrayList<>();
    private ArrayAdapter<String> languageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        String userId = sessionManager.getUserUid();
        if (userId.equals("Guest")) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupSpinners();
        loadLanguages();
        loadUserData(userId);
        setupActionButtons(userId);
    }

    private void setupViews() {
        binding.edtCurrentPass.setVisibility(View.GONE);
        binding.edtNewPass.setVisibility(View.GONE);
        binding.edtConfirmPass.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        List<String> genderOptions = Arrays.asList("Male", "Female", "Other");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.edtGender.setAdapter(genderAdapter);

        languageAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.edtLanguage.setAdapter(languageAdapter);
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

                if (currentUser != null && currentUser.getLanguage() != null) {
                    setSpinnerSelection(binding.edtLanguage, getLanguageName(currentUser.getLanguage()));
                }
            }
        });
    }

    private void loadUserData(String userId) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        viewModel.loadUserData(userId);
        viewModel.getUserData().observe(this, user -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            if (user != null) {
                currentUser = user;
                binding.edtName.setText(user.getName() != null ? user.getName() : "");
                binding.edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                binding.edtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                binding.edtDob.setText(user.getDate() != null ? user.getDate() : "");
                binding.edtAddress.setText(user.getAddress() != null ? user.getAddress() : "");

                if (user.getGender() != null) {
                    setSpinnerSelection(binding.edtGender, user.getGender());
                }

                if (user.getLanguage() != null && !languageList.isEmpty()) {
                    setSpinnerSelection(binding.edtLanguage, getLanguageName(user.getLanguage()));
                }

            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupActionButtons(String userId) {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.btnTogglePass.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            int visibility = isPasswordVisible ? View.VISIBLE : View.GONE;
            binding.edtCurrentPass.setVisibility(visibility);
            binding.edtNewPass.setVisibility(visibility);
            binding.edtConfirmPass.setVisibility(visibility);
        });

        binding.btnUpdate.setOnClickListener(v -> saveUserData(userId));
    }

    private void saveUserData(String userId) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        String name = binding.edtName.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String dob = binding.edtDob.getText().toString().trim();
        String address = binding.edtAddress.getText().toString().trim();
        String gender = binding.edtGender.getSelectedItem() != null ?
                binding.edtGender.getSelectedItem().toString() : "";
        String language = binding.edtLanguage.getSelectedItem() != null ?
                getLanguageId(binding.edtLanguage.getSelectedItem().toString()) : "";

        if (name.isEmpty()) {
            binding.edtName.setError("Name cannot be empty");
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        if (email.isEmpty()) {
            binding.edtEmail.setError("Email cannot be empty");
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            binding.edtEmail.setError("Invalid email format");
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        if (language.isEmpty()) {
            Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            return;
        }

        UserModel user = new UserModel();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setDate(dob);
        user.setGender(gender);
        user.setAddress(address);
        user.setLanguage(language);
        user.setUsername(currentUser != null && currentUser.getUsername() != null ? currentUser.getUsername() : "");

        if (isPasswordVisible) {
            String currentPass = binding.edtCurrentPass.getText().toString().trim();
            String newPass = binding.edtNewPass.getText().toString().trim();
            String confirmPass = binding.edtConfirmPass.getText().toString().trim();

            if (currentPass.isEmpty()) {
                binding.edtCurrentPass.setError("Current password cannot be empty");
                if (binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                return;
            }
            if (newPass.isEmpty()) {
                binding.edtNewPass.setError("New password cannot be empty");
                if (binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                return;
            }
            if (newPass.length() < 6) {
                binding.edtNewPass.setError("New password must be at least 6 characters");
                if (binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                return;
            }
            if (!newPass.equals(confirmPass)) {
                binding.edtConfirmPass.setError("Passwords do not match");
                if (binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                return;
            }

            viewModel.updateUserData(userId, user).observe(this, profileSuccess -> {
                if (!profileSuccess) {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    return;
                }

                sessionManager.saveUserSession(userId, currentUser != null ? currentUser.getId() : 0, language);

                viewModel.checkAndUpdatePassword(userId, currentPass, newPass).observe(this, result -> {
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    if ("Success".equals(result)) {
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        binding.edtCurrentPass.setText("");
                        binding.edtNewPass.setText("");
                        binding.edtConfirmPass.setText("");
                        isPasswordVisible = false;
                        binding.edtCurrentPass.setVisibility(View.GONE);
                        binding.edtNewPass.setVisibility(View.GONE);
                        binding.edtConfirmPass.setVisibility(View.GONE);
                        startActivity(new Intent(this, SettingActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            viewModel.updateUserData(userId, user).observe(this, success -> {
                if (binding.progressBar != null) {
                    binding.progressBar.setVisibility(View.GONE);
                }
                if (success) {
                    sessionManager.saveUserSession(userId, currentUser != null ? currentUser.getId() : 0, language);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, SettingActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setSpinnerSelection(android.widget.Spinner spinner, String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private String getLanguageId(String languageName) {
        for (LanguageModel lang : languageList) {
            if (lang.getName().equals(languageName)) {
                return lang.getId();
            }
        }
        return "";
    }

    private String getLanguageName(String languageId) {
        for (LanguageModel lang : languageList) {
            if (lang.getId().equals(languageId)) {
                return lang.getName();
            }
        }
        return "";
    }
}