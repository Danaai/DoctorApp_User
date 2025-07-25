package com.example.doctorapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.doctorapp.Adapter.DateAdapter;
import com.example.doctorapp.Adapter.TimeAdapter;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.Domain.DoctorScheduleModel;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.Domain.NotificationModel;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.databinding.ActivityDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private ActivityDetailBinding binding;
    private DoctorsModel item;
    private DoctorScheduleModel doctorSchedule;
    private boolean isFavorite = false;
    private DatabaseReference favoritesRef;
    private DatabaseReference apptRef;
    private DatabaseReference schedulesRef;
    private String userUid;
    private String selectedDate;
    private String selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userUid = prefs.getString("userUid", "Guest");
        Log.d(TAG, "Current UID: " + userUid);

        item = (DoctorsModel) getIntent().getSerializableExtra("object");
        if (item == null) {
            Toast.makeText(this, "Doctor not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        favoritesRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_FAVORITES).child(userUid);
        apptRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_APPOINTMENTS).child(userUid);
        schedulesRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_SCHEDULES).child(String.valueOf(item.getId()));

        setVariable();
        initDate();
        initTime(null);
        checkFavoriteStatus();

        binding.bookBtn.setOnClickListener(v -> bookAppointment());
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private String parseDisplayDateToDbFormat(String displayDate) {
        try {
            int currentYear = LocalDate.now().getYear();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE-dd-MMM-yyyy", Locale.US);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
            LocalDate parsedDate = LocalDate.parse(displayDate + "-" + currentYear, inputFormatter);
            return parsedDate.format(outputFormatter);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing date: " + displayDate, e);
            return null;
        }
    }

    private void initDate() {
        binding.dateView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DateAdapter dateAdapter = new DateAdapter(new ArrayList<>());
        binding.dateView.setAdapter(dateAdapter);
        dateAdapter.setOnItemClickListener(date -> {
            selectedDate = date;
            initTime(selectedDate);
        });

        generateDates(dateAdapter);
    }

    private void generateDates(DateAdapter dateAdapter) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEE-dd-MMM", Locale.US);
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        LocalDate currentDate = LocalDate.now();

        schedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                    DoctorScheduleModel schedule = scheduleSnapshot.getValue(DoctorScheduleModel.class);
                    if (schedule != null) {
                        try {
                            LocalDate scheduleDate = LocalDate.parse(schedule.getDate(), dbFormatter);
                            if (scheduleDate.isAfter(currentDate)) {
                                dates.add(scheduleDate.format(displayFormatter));
                            }
                        } catch (DateTimeParseException e) {
                            Log.e(TAG, "Error parsing schedule date: " + e.getMessage());
                        }
                    }
                }
                if (dates.isEmpty()) {
                    Toast.makeText(DetailActivity.this, "Bác sĩ không có lịch làm việc", Toast.LENGTH_SHORT).show();
                }
                dateAdapter.updateDates(dates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading schedules: " + error.getMessage());
                Toast.makeText(DetailActivity.this, "Lỗi tải lịch làm việc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTime(String date) {
        binding.timeView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<String> timeSlots = new ArrayList<>();
        TimeAdapter timeAdapter = new TimeAdapter(timeSlots);
        binding.timeView.setAdapter(timeAdapter);
        timeAdapter.setOnItemClickListener(time -> selectedTime = time);

        if (date == null) {
            timeAdapter.setBookedTimes(new ArrayList<>());
            return;
        }

        String formattedDate = parseDisplayDateToDbFormat(date);
        if (formattedDate == null) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        schedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorSchedule = null;
                timeSlots.clear();

                for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                    DoctorScheduleModel schedule = scheduleSnapshot.getValue(DoctorScheduleModel.class);
                    if (schedule != null && schedule.getDate().equals(formattedDate)) {
                        doctorSchedule = schedule;
                        break;
                    }
                }
                if (doctorSchedule != null) {
                    timeSlots.addAll(generateTimeSlots(doctorSchedule.getStartTime(), doctorSchedule.getEndTime()));
                } else {
                    Toast.makeText(DetailActivity.this, "Bác sĩ không có lịch làm việc vào ngày này", Toast.LENGTH_SHORT).show();
                }
                timeAdapter.notifyDataSetChanged();

                DatabaseReference allApptRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_APPOINTMENTS);
                allApptRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> bookedTimes = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            for (DataSnapshot apptSnapshot : userSnapshot.getChildren()) {
                                Long doctorIdLong = apptSnapshot.child("doctorId").getValue(Long.class);
                                String apptDate = apptSnapshot.child("date").getValue(String.class);
                                String time = apptSnapshot.child("time").getValue(String.class);
                                String status = apptSnapshot.child("status").getValue(String.class);

                                if (doctorIdLong != null && doctorIdLong.intValue() == item.getId() &&
                                        apptDate != null && apptDate.equals(formattedDate) &&
                                        time != null && !status.equals("CANCELED")) {
                                    bookedTimes.add(time);
                                }
                            }
                        }
                        timeAdapter.setBookedTimes(bookedTimes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading appointments: " + error.getMessage());
                        Toast.makeText(DetailActivity.this, "Lỗi tải lịch đã đặt", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading schedules: " + error.getMessage());
                Toast.makeText(DetailActivity.this, "Lỗi tải lịch làm việc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static List<String> generateTimeSlots(String startTime, String endTime) {
        List<String> timeSlots = new ArrayList<>();
        try {
            LocalTime start = LocalTime.parse(startTime, TIME_FORMAT);
            LocalTime end = LocalTime.parse(endTime, TIME_FORMAT);
            if (start.isAfter(end)) {
                Log.e(TAG, "Invalid time range: start=" + startTime + ", end=" + endTime);
                return timeSlots;
            }

            LocalTime current = start;
            while (!current.isAfter(end)) {
                timeSlots.add(current.format(TIME_FORMAT));
                current = current.plusHours(2);
            }
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing time: start=" + startTime + ", end=" + endTime, e);
        }
        return timeSlots;
    }

    private void setVariable() {
        Glide.with(this)
                .load(item.getPicture())
                .into(binding.img);

        binding.addressTxt.setText(item.getAddress());
        binding.nameTxt.setText(item.getName());
        binding.patientsTxt.setText(String.valueOf(item.getPatients()));
        binding.experienceTxt.setText(item.getExperience() + " Years");

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category")
                .child(String.valueOf(item.getSpecial())).child("Name");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.specialTxt.setText(snapshot.getValue(String.class));
                } else {
                    binding.specialTxt.setText("Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.specialTxt.setText("Error");
            }
        });

        binding.favoriteBtn.setOnClickListener(v -> toggleFavorite());
    }

    private void checkFavoriteStatus() {
        if (userUid.equals("Guest") || item == null) return;

        favoritesRef.child(String.valueOf(item.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                binding.favoriteBtn.setImageResource(isFavorite ? R.drawable.favorite_black : R.drawable.favorite_white);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking favorite status: " + error.getMessage());
            }
        });
    }

    private void toggleFavorite() {
        if (userUid.equals("Guest") || item == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        DatabaseReference favoriteRef = favoritesRef.child(String.valueOf(item.getId()));
        if (isFavorite) {
            favoriteRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        binding.favoriteBtn.setImageResource(R.drawable.favorite_white);
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show());
        } else {
            favoriteRef.setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        binding.favoriteBtn.setImageResource(R.drawable.favorite_black);
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show());
        }
    }

    private void bookAppointment() {
        TimeAdapter timeAdapter = (TimeAdapter) binding.timeView.getAdapter();
        DateAdapter dateAdapter = (DateAdapter) binding.dateView.getAdapter();
        selectedDate = dateAdapter.getSelectedDate();
        selectedTime = timeAdapter.getSelectedTime();
        String reason = binding.edtReason.getText().toString().trim();

        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Vui lòng chọn ngày và giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            binding.edtReason.setError("Reason required");
            return;
        }

        if (userUid.equals("Guest")) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String formattedDate = parseDisplayDateToDbFormat(selectedDate);
        if (formattedDate == null) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        String appointmentId = apptRef.push().getKey();
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US));
        AppointmentModel appointment = new AppointmentModel( item.getId(), formattedDate, selectedTime,
                AppointmentStatus.UPCOMING, reason, createdAt,
                "Please arrive 15 minutes early for your appointment to complete check-in. Bring all necessary identification and previous medical records (if any). Thank you!"
        );

        apptRef.child(appointmentId).setValue(appointment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(appointmentId);
                Toast.makeText(this, "Book successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AppointmentsActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Book failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String appointmentId) {
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userUid);
        String notiId = notiRef.push().getKey();
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US));
        String formattedDate = parseDisplayDateToDbFormat(selectedDate);
        String message = "Appointment with " + item.getName() + " on " +
                (formattedDate != null ? formattedDate : "Unknown Date") + " " + selectedTime + " (" + AppointmentStatus.UPCOMING + ")";
        NotificationModel notification = new NotificationModel(message, createdAt, false, appointmentId);
        notiRef.child(notiId).setValue(notification);
        Log.d(TAG, "Notification sent: " + message);
    }
}