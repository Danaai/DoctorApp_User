package com.example.doctorapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.doctorapp.R;
import com.example.doctorapp.databinding.ActivityHelpSecurityBinding;

import java.io.IOException;

public class HelpSecurityActivity extends AppCompatActivity {
    private ActivityHelpSecurityBinding binding;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHelpSecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = prefs.getString("userUid", "Guest");

        binding.backBtn.setOnClickListener(v -> onBackPressed());

        binding.contactBtn.setOnClickListener(v -> {

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:vdai20204@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from Doctor App User: " + userId);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear support team, \n\nI need assistance with the following issue: \n\n");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "No email app found. Please contact us at support@doctorapp.com", Toast.LENGTH_LONG).show();
            }

        });

    }
}