package com.example.doctorapp.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.example.doctorapp.R;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.databinding.ActivitySettingBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingActivity extends BaseActivity {
    private ActivitySettingBinding binding;
    private SessionManager sessionManager;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (bottomNavigationBinding != null) {
            bottomNavigationBinding.settingImg.setColorFilter(Color.WHITE);
            bottomNavigationBinding.navSettings.setBackgroundColor(
                    getResources().getColor(R.color.blue));
        }

        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserUid();

        if (userId.equals("Guest")) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference(
                Constants.DB_PATH_USERS).child(userId);

        loadUserProfile();
        setupActionButtons();
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String dob = snapshot.child("date").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    binding.userNameTxt.setText(name != null && !name.isEmpty() ? name : "N/A");
                    binding.emailTxt.setText(email != null && !email.isEmpty() ? email : "N/A");
                    binding.phoneTxt.setText(phone != null && !phone.isEmpty() ? phone : "N/A");
                    binding.dateTxt.setText(dob != null && !dob.isEmpty() ? dob : "N/A");
                    binding.genderTxt.setText(gender != null && !gender.isEmpty() ? gender : "N/A");
                    binding.addressTxt.setText(address != null && !address.isEmpty() ? address : "N/A");
                } else {
                    Toast.makeText(SettingActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(SettingActivity.this, error, "Failed to load user data");
            }
        });
    }

    private void setupActionButtons() {
        binding.editBtn.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
        binding.languageText.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageActivity.class)));
        binding.helpText.setOnClickListener(v ->
                startActivity(new Intent(this, HelpSecurityActivity.class)));
        binding.logoutBtn.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}