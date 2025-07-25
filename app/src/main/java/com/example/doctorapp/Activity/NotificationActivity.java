package com.example.doctorapp.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.doctorapp.Adapter.NotificationPopupAdapter;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.NotificationModel;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.databinding.ActivityNotificationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {
    private ActivityNotificationBinding binding;
    private NotificationPopupAdapter adapter;
    private SessionManager sessionManager;
    private List<NotificationModel> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (bottomNavigationBinding != null) {
            bottomNavigationBinding.notificationImg.setColorFilter(Color.WHITE);
            bottomNavigationBinding.navNotification.setBackgroundColor(
                    getResources().getColor(R.color.blue));
        }

        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserUid();

        if (userId.equals("Guest")) {
            Toast.makeText(this, "Please log in to view notifications!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView(userId);
        loadNotifications(userId);
    }

    private void setupRecyclerView(String userId) {
        adapter = new NotificationPopupAdapter(userId, this);
        binding.notificationList.setLayoutManager(new LinearLayoutManager(this));
        binding.notificationList.setAdapter(adapter);
    }

    private void loadNotifications(String userId) {
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference(
                Constants.DB_PATH_NOTIFICATIONS).child(userId);

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    NotificationModel model = childSnapshot.getValue(NotificationModel.class);
                    if (model != null) {
                        model.setId(childSnapshot.getKey());
                        notificationList.add(model);
                    }
                }
                adapter.setNotifications(notificationList);
                binding.loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.loadingIndicator.setVisibility(View.GONE);
                FirebaseErrorHandler.handleError(NotificationActivity.this, error, "Failed to load notifications");
            }
        });
    }
}