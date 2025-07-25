package com.example.doctorapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.Adapter.CategoryAdapter;
import com.example.doctorapp.Adapter.NotificationPopupAdapter;
import com.example.doctorapp.Adapter.TopDoctorsAdapter;
import com.example.doctorapp.Domain.NotificationModel;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private SessionManager sessionManager;
    private CategoryAdapter categoryAdapter;
    private TopDoctorsAdapter doctorAdapter;
    private DatabaseReference userRef;
    private String userUid;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private PopupWindow notificationPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (bottomNavigationBinding != null) {
            bottomNavigationBinding.homeImg.setColorFilter(Color.WHITE);
            bottomNavigationBinding.navHome.setBackgroundColor(
                    getResources().getColor(R.color.blue));
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        binding.profileImage.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.notificationImg.setOnClickListener(v -> showNotificationPopup(v));

        sessionManager = new SessionManager(this);
        viewModel = new MainViewModel();

        userRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_USERS);
        userUid = sessionManager.getUserUid();

        setupUserView();
        setupRecyclerViews();
        loadUserData();
        loadCategories();
        loadDoctors();
        updateNavHeader();
    }

    private void setupUserView() {
        if (userUid.equals("Guest")) {
            binding.userNameTxt.setText("Guest");
        }
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this, new ArrayList<>());
        binding.catView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.catView.setAdapter(categoryAdapter);

        doctorAdapter = new TopDoctorsAdapter(this, new ArrayList<>());
        binding.doctorView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.doctorView.setAdapter(doctorAdapter);

        binding.doctorListTxt.setOnClickListener(v ->
                startActivity(new Intent(this, TopDoctorsActivity.class)));
    }

    private void loadUserData() {
        if (userUid.equals("Guest")) return;

        userRef.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    binding.userNameTxt.setText(name != null ? "Hi, " + name : "N/A");

                    // Cập nhật NavigationView header
                    TextView navUserName = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
                    TextView navUserEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);
                    navUserName.setText(name != null ? name : "Unknown User");
                    navUserEmail.setText(email != null ? email : "N/A");
                } else {
                    Log.e("MainActivity", "User not found in database for UID: " + userUid);
                    Toast.makeText(MainActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(MainActivity.this, error, "Failed to load user data");
            }
        });
    }

    private void loadCategories() {
        viewModel.loadCategories().observe(this, list -> {
            if (list != null) {
                categoryAdapter.updateList(list);
            }
            binding.progressBarCat.setVisibility(View.GONE);
        });
    }

    private void loadDoctors() {
        viewModel.loadDoctors().observe(this, doctors -> {
            if (doctors != null) {
                doctorAdapter.updateList(doctors);
            }
            binding.progressBarDoctor.setVisibility(View.GONE);
        });
    }

    private void updateNavHeader() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userName = prefs.getString("userName", "Unknown User");
        String userEmail = prefs.getString("userEmail", "N/A");

        TextView navUserName = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
        TextView navUserEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);
        navUserName.setText(userName);
        navUserEmail.setText(userEmail);
    }

    private void showNotificationPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.notification_popup, null);

        notificationPopup = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        RecyclerView notificationList = popupView.findViewById(R.id.notification_list);
        ProgressBar loadingIndicator = popupView.findViewById(R.id.loading_indicator);
        notificationList.setLayoutManager(new LinearLayoutManager(this));

        if (userUid == null || userUid.equals("Guest")) {
            Log.e("NotificationPopup", "User UID is null or Guest, cannot load notifications");
            loadingIndicator.setVisibility(View.GONE);
            Toast.makeText(this, "Cannot load notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationPopupAdapter adapter = new NotificationPopupAdapter(userUid, this);
        notificationList.setAdapter(adapter);

        notificationPopup.showAsDropDown(anchorView, 70, 34);

        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_NOTIFICATIONS).child(userUid);
        notiRef.orderByChild("createdAt").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("NotificationsPopup", "Snapshot exists: " + snapshot.exists() + " , Children count: " + snapshot.getChildrenCount());
                List<NotificationModel> items = new ArrayList<>();
                for (DataSnapshot notifSnapshot : snapshot.getChildren()) {
                    String id = notifSnapshot.getKey();
                    String message = notifSnapshot.child("message").getValue(String.class);
                    Boolean isRead = notifSnapshot.child("isRead").getValue(Boolean.class);
                    String createdAt = notifSnapshot.child("createdAt").getValue(String.class);
                    String appointmentId = notifSnapshot.child("appointmentId").getValue(String.class);

                    Log.d("NotificationPopup", "Message: " + message + ", CreatedAt: " + createdAt + ", isRead: " + isRead + ", appointmentId: " + appointmentId);

                    if (message != null && createdAt != null && isRead != null && appointmentId != null) {
                        items.add(new NotificationModel(id, message, createdAt, isRead, appointmentId));
                    }
                }
                adapter.setNotifications(items);
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationPopup", "Error: " + error.getMessage());
                FirebaseErrorHandler.handleError(MainActivity.this, error, "Failed to load notifications");
                loadingIndicator.setVisibility(View.GONE);
            }
        });

        TextView notificationsTxt = popupView.findViewById(R.id.notificationsTxt);
        notificationsTxt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
            notificationPopup.dismiss();
        });

        popupView.setOnTouchListener((v, event) -> {
            notificationPopup.dismiss();
            return true;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (id == R.id.nav_appointments) {
            startActivity(new Intent(this, AppointmentsActivity.class));
        } else if (id == R.id.nav_doctors) {
            startActivity(new Intent(this, TopDoctorsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (id == R.id.nav_logout) {
            sessionManager.clearSession();
            Toast.makeText(this, "Logout Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, true);
        } else {
            super.onBackPressed();
        }
    }
}