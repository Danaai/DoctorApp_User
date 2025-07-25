package com.example.doctorapp.Activity;

import static com.example.doctorapp.Domain.AppointmentStatus.CANCELED;
import static com.example.doctorapp.Domain.AppointmentStatus.COMPLETED;
import static com.example.doctorapp.Domain.AppointmentStatus.UPCOMING;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityAppointmentDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppointmentDetailActivity extends BaseActivity {
    private ActivityAppointmentDetailBinding binding;
    private MainViewModel viewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAppointmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Intent intent = getIntent();
        AppointmentModel appointment = (AppointmentModel) intent.getSerializableExtra("appointment");

        if (appointment == null) {
            Toast.makeText(this, "Appointment not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDoctorData(appointment.getDoctorId());
        setupAppointmentDetails(appointment);
        setupActionButtons(appointment);
    }

    private void loadDoctorData(int doctorId) {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance()
                .getReference(Constants.DB_PATH_DOCTORS).child(String.valueOf(doctorId));

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.child("Picture").getValue(String.class);
                    String name = snapshot.child("Name").getValue(String.class);
                    Long specialId = snapshot.child("Special").getValue(Long.class);
                    String phone = snapshot.child("Mobile").getValue(String.class);
                    String mail = snapshot.child("Email").getValue(String.class);

                    Glide.with(AppointmentDetailActivity.this)
                            .load(imageUrl)
                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.profile))
                            .into(binding.doctorImage);

                    binding.doctorNameTxt.setText(name != null ? name : "N/A");
                    binding.doctorPhoneTxt.setText(phone != null ? "+" + phone : "N/A");
                    binding.doctorEmailTxt.setText(mail != null ? mail : "N/A");

                    if (specialId != null) {
                        DatabaseReference categoryRef = FirebaseDatabase.getInstance()
                                .getReference(Constants.DB_PATH_CATEGORIES).child(String.valueOf(specialId));
                        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                                if (categorySnapshot.exists()) {
                                    String specialName = categorySnapshot.child("Name").getValue(String.class);
                                    binding.doctorSpecialTxt.setText(specialName != null ? specialName : "N/A");
                                } else {
                                    binding.doctorSpecialTxt.setText("N/A");
                                    Log.e("AppointmentDetail", "Category not found for ID: " + specialId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                FirebaseErrorHandler.handleError(AppointmentDetailActivity.this, error,
                                        "Failed to load specialty name");
                                binding.doctorSpecialTxt.setText("N/A");
                            }
                        });
                    } else {
                        binding.doctorSpecialTxt.setText("N/A");
                    }
                } else {
                    binding.doctorNameTxt.setText("N/A");
                    binding.doctorPhoneTxt.setText("N/A");
                    binding.doctorEmailTxt.setText("N/A");
                    binding.doctorSpecialTxt.setText("N/A");
                    Log.e("AppointmentDetail", "Doctor not found for ID: " + doctorId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(AppointmentDetailActivity.this, error, "Failed to load doctor data");
                binding.doctorNameTxt.setText("N/A");
                binding.doctorPhoneTxt.setText("N/A");
                binding.doctorEmailTxt.setText("N/A");
                binding.doctorSpecialTxt.setText("N/A");
            }
        });
    }

    private void setupAppointmentDetails(AppointmentModel appointment) {
        binding.appointmentCodeTxt.setText((appointment.getAppointmentId() != null ? appointment.getAppointmentId() : "N/A"));
        binding.dateTxt.setText((appointment.getDate() != null ? appointment.getDate() : "N/A"));
        binding.timeTxt.setText((appointment.getTime() != null ? appointment.getTime() : "N/A"));
        binding.reasonTxt.setText((appointment.getReason() != null ? appointment.getReason() : "N/A"));
        binding.createdAtTxt.setText((appointment.getCreatedAt() != null ? appointment.getCreatedAt() : "N/A"));
        binding.notesTxt.setText((appointment.getNotes() != null ? appointment.getNotes() : "N/A"));

        if (appointment.getStatus() != null) {
            binding.statusTxt.setText("⬤ " + appointment.getStatus().toString());
            switch (appointment.getStatus()) {
                case UPCOMING:
                    binding.statusTxt.setBackgroundResource(R.drawable.status_upcoming_bg);
                    break;
                case CANCELED:
                    binding.statusTxt.setBackgroundResource(R.drawable.status_cancelled_bg);
                    break;
                case COMPLETED:
                    binding.statusTxt.setBackgroundResource(R.drawable.status_confirmed_bg);
                    break;
                default:
                    binding.statusTxt.setBackgroundResource(R.drawable.blue_btn_bg);
                    break;
            }
        } else {
            binding.statusTxt.setText("⬤ UNKNOWN");
            binding.statusTxt.setBackgroundResource(R.drawable.blue_btn_bg);
        }
    }

    private void setupActionButtons(AppointmentModel appointment) {
        if (appointment.getStatus() == UPCOMING) {
            binding.cancelBtn.setVisibility(View.VISIBLE);
            binding.cancelBtn.setOnClickListener(v -> {
                String userId = sessionManager.getUserUid();
                if (userId == null || userId.equals("Guest")) {
                    Toast.makeText(this, "Guest users cannot cancel appointments", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (appointment.getAppointmentId() == null || appointment.getAppointmentId().isEmpty()) {
                    Toast.makeText(this, "Invalid appointment ID", Toast.LENGTH_SHORT).show();
                    Log.e("AppointmentDetail", "Appointment ID is null or empty");
                    return;
                }
                viewModel.cancelAppointmentAndUpdateNotification(userId, appointment.getAppointmentId())
                        .observe(this, success -> {
                            if (success) {
                                Toast.makeText(this, "Appointment canceled", Toast.LENGTH_SHORT).show();
                                appointment.setStatus(AppointmentStatus.CANCELED);
                                setupAppointmentDetails(appointment);
                                setupActionButtons(appointment);
                            } else {
                                Toast.makeText(this, "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        } else {
            binding.cancelBtn.setVisibility(View.GONE);
        }

        binding.backBtn.setOnClickListener(v -> finish());
        binding.contactBtn.setOnClickListener(v -> {
            String phone = binding.doctorPhoneTxt.getText().toString();
            String email = binding.doctorEmailTxt.getText().toString();

            if (!phone.equals("N/A")) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone));
                startActivity(callIntent);
            } else if (!email.equals("N/A")) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + email));
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            } else {
                Toast.makeText(this, "Không có thông tin liên hệ!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}