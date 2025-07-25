package com.example.doctorapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doctorapp.R;
import com.example.doctorapp.databinding.BottomNavigationBinding;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationBinding bottomNavigationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initializeBottomNavigation(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initializeBottomNavigation(findViewById(android.R.id.content));
    }

    private void initializeBottomNavigation(View rootView) {
        if (rootView == null) {
            Log.e("BaseActivity", "Root view is null");
            return;
        }
        View navigationLayout = rootView.findViewById(R.id.bottom_navigation_layout);
        if (navigationLayout != null) {
            bottomNavigationBinding = BottomNavigationBinding.bind(navigationLayout);
            if (bottomNavigationBinding != null) {
                Log.d("BaseActivity", "bottomNavigationBinding initialized successfully");
                setupBottomNavigation();
            } else {
                Log.e("BaseActivity", "Failed to initialize bottomNavigationBinding");
            }
        } else {
            Log.e("BaseActivity", "bottom_navigation_layout not found in root view");
        }
    }

    protected void setupBottomNavigation() {
        if (bottomNavigationBinding == null) {
            Log.e("BaseActivity", "bottomNavigationBinding is null");
            return;
        }

        bottomNavigationBinding.navHome.setOnClickListener(v -> {
            Log.d("BottomNav", "Home button clicked");
            if (!(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        bottomNavigationBinding.navAppointment.setOnClickListener(v -> {
            Log.d("BottomNav", "Appointment button clicked");
            if (!(this instanceof AppointmentsActivity)) {
                startActivity(new Intent(this, AppointmentsActivity.class));
                finish();
            }
        });

        bottomNavigationBinding.navNotification.setOnClickListener(v -> {
            Log.d("BottomNav", "Notification button clicked");
            if (!(this instanceof NotificationActivity)) {
                startActivity(new Intent(this, NotificationActivity.class));
                finish();
            }
        });

        bottomNavigationBinding.navSettings.setOnClickListener(v -> {
            Log.d("BottomNav", "Settings button clicked");
            if (!(this instanceof SettingActivity)) {
                startActivity(new Intent(this, SettingActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bottomNavigationBinding = null;
    }
}