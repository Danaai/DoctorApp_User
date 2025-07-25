package com.example.doctorapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.Domain.UserModel;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.Domain.CategoryModel;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public LiveData<List<CategoryModel>> loadCategories() {
        final MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_CATEGORIES);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if (item != null) {
                        lists.add(item);
                    }
                }
                listData.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load categories");
            }
        });
        return listData;
    }

    public LiveData<List<DoctorsModel>> loadDoctors() {
        final MutableLiveData<List<DoctorsModel>> liveData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_DOCTORS);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DoctorsModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    DoctorsModel item = childSnapshot.getValue(DoctorsModel.class);
                    if (item != null) {
                        lists.add(item);
                    }
                }
                liveData.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load doctors");
            }
        });
        return liveData;
    }

    public LiveData<Map<Integer, String>> loadCategoryNames() {
        MutableLiveData<Map<Integer, String>> categoryNames = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_CATEGORIES);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, String> map = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Integer id = Integer.parseInt(child.getKey());
                        String name = child.child("Name").getValue(String.class);
                        if (name != null) {
                            map.put(id, name);
                        }
                    } catch (NumberFormatException e) {
                        FirebaseErrorHandler.handleError(null, null, "Invalid category key: " + child.getKey());
                    }
                }
                categoryNames.setValue(map);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load category names");
            }
        });
        return categoryNames;
    }

    public LiveData<Map<String, Object>> loadDoctorMaps() {
        MutableLiveData<Map<String, Object>> doctorMaps = new MutableLiveData<>();
        MutableLiveData<Map<Long, String>> categoryMapLiveData = new MutableLiveData<>();
        DatabaseReference categoriesRef = firebaseDatabase.getReference(Constants.DB_PATH_CATEGORIES);
        DatabaseReference doctorsRef = firebaseDatabase.getReference(Constants.DB_PATH_DOCTORS);

        // Load category map first
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Long, String> categoryMap = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Long id = child.child("Id").getValue(Long.class);
                    String name = child.child("Name").getValue(String.class);
                    if (id != null && name != null) {
                        categoryMap.put(id, name);
                    }
                }
                categoryMapLiveData.setValue(categoryMap);

                // Load doctors after categories
                doctorsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot doctorSnapshot) {
                        Map<String, Object> result = new HashMap<>();
                        Map<String, String> nameMap = new HashMap<>();
                        Map<String, String> specializationMap = new HashMap<>();
                        Map<String, String> imageMap = new HashMap<>();

                        for (DataSnapshot child : doctorSnapshot.getChildren()) {
                            String doctorId = child.getKey();
                            String name = child.child("Name").getValue(String.class);
                            Long specializationId = child.child("Special").getValue(Long.class);
                            String imageUrl = child.child("Picture").getValue(String.class);

                            if (doctorId != null) {
                                nameMap.put(doctorId, name != null ? name : "Unknown Doctor");
                                String specialization = specializationId != null ?
                                        categoryMap.getOrDefault(specializationId, "Unknown Specialty") : "Unknown Specialty";
                                specializationMap.put(doctorId, specialization);
                                imageMap.put(doctorId, imageUrl != null ? imageUrl : "");
                            }
                        }

                        result.put("nameMap", nameMap);
                        result.put("specializationMap", specializationMap);
                        result.put("imageMap", imageMap);
                        doctorMaps.setValue(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        FirebaseErrorHandler.handleError(null, error, "Failed to load doctor maps");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load categories for doctor maps");
            }
        });
        return doctorMaps;
    }

    public LiveData<List<AppointmentModel>> loadAppointments(String userId) {
        MutableLiveData<List<AppointmentModel>> appointments = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_APPOINTMENTS).child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AppointmentModel> appointmentList = new ArrayList<>();
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    String appointmentId = appointmentSnapshot.getKey();
                    try {
                        if (!appointmentSnapshot.hasChild("doctorId") || !appointmentSnapshot.hasChild("status")) {
                            continue;
                        }

                        Long doctorIdLong = getLongValue(appointmentSnapshot.child("doctorId"), appointmentId, "doctorId");

                        // Parse date
                        Object dateObj = appointmentSnapshot.child("date").getValue();
                        String date = parseStringValue(dateObj, appointmentId, "date");

                        // Parse time
                        Object timeObj = appointmentSnapshot.child("time").getValue();
                        String time = parseStringValue(timeObj, appointmentId, "time");

                        // Parse status
                        Object statusObj = appointmentSnapshot.child("status").getValue();
                        if (statusObj == null) {
                            continue;
                        }
                        String statusStr = parseStatusValue(statusObj, appointmentId);
                        AppointmentStatus status;
                        try {
                            status = AppointmentStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        // Parse reason
                        Object reasonObj = appointmentSnapshot.child("reason").getValue();
                        String reason = parseStringOrNumber(reasonObj, appointmentId, "reason");

                        // Parse createdAt
                        Object createdAtObj = appointmentSnapshot.child("createdAt").getValue();
                        String createdAt = parseStringOrNumber(createdAtObj, appointmentId, "createdAt");

                        // Parse notes
                        Object notesObj = appointmentSnapshot.child("notes").getValue();
                        String notes = parseStringOrNumber(notesObj, appointmentId, "notes");

                        if (appointmentId == null) {
                            continue;
                        }

                        int doctorId = doctorIdLong != null ? doctorIdLong.intValue() : -1;

                        AppointmentModel appointment = new AppointmentModel(
                                appointmentId, doctorId, date, time, status, reason, createdAt, notes);
                        appointmentList.add(appointment);
                    } catch (Exception e) {
                        // Log error but continue processing other appointments
                    }
                }
                appointments.setValue(appointmentList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load appointments");
            }
        });
        return appointments;
    }

    public LiveData<List<LanguageModel>> loadLanguages() {
        MutableLiveData<List<LanguageModel>> languages = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_LANGUAGES);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LanguageModel> languageList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    LanguageModel language = childSnapshot.getValue(LanguageModel.class);
                    if (language != null && language.getId() != null && language.getName() != null && language.getImg() != null) {
                        languageList.add(language);
                    }
                }
                languages.setValue(languageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load languages");
            }
        });
        return languages;
    }

    public LiveData<UserModel> loadUserData(String userId) {
        MutableLiveData<UserModel> userData = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_USERS).child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                userData.setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to load user data");
                userData.setValue(null);
            }
        });
        return userData;
    }

    public LiveData<Boolean> updateUserData(String userId, UserModel user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_USERS).child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", user.getName());
        updates.put("email", user.getEmail());
        updates.put("phone", user.getPhone());
        updates.put("date", user.getDate());
        updates.put("gender", user.getGender());
        updates.put("address", user.getAddress());
        updates.put("username", user.getUsername());
        if (user.getPassword() != null) {
            updates.put("password", user.getPassword());
        }

        ref.updateChildren(updates)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    public LiveData<Boolean> updateAppointmentStatus(String userId, String appointmentId, AppointmentStatus newStatus) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        if (appointmentId == null || appointmentId.isEmpty()) {
            FirebaseErrorHandler.handleError(null, null, "Invalid appointment ID: " + appointmentId);
            result.setValue(false);
            return result;
        }

        if (userId == null || userId.equals("Guest")) {
            FirebaseErrorHandler.handleError(null, null, "Invalid user ID or guest user: " + userId);
            result.setValue(false);
            return result;
        }

        DatabaseReference appointmentRef = firebaseDatabase.getReference(Constants.DB_PATH_APPOINTMENTS)
                .child(userId).child(appointmentId);

        appointmentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    appointmentRef.child("status").setValue(newStatus.toString())
                            .addOnSuccessListener(aVoid -> result.setValue(true))
                            .addOnFailureListener(e -> {
                                result.setValue(false);
                            });
                } else {
                    FirebaseErrorHandler.handleError(null, null, "Appointment not found for ID: " + appointmentId);
                    result.setValue(false);
                }
            } else {
                result.setValue(false);
            }
        });

        return result;
    }

    public LiveData<Boolean> updateNotification(String userId, String appointmentId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        DatabaseReference notificationsRef = firebaseDatabase.getReference(Constants.DB_PATH_NOTIFICATIONS).child(userId);
        DatabaseReference appointmentRef = firebaseDatabase.getReference(Constants.DB_PATH_APPOINTMENTS).child(userId).child(appointmentId);

        appointmentRef.get().addOnCompleteListener(appointmentTask -> {
            if (appointmentTask.isSuccessful()) {
                DataSnapshot appointmentSnapshot = appointmentTask.getResult();
                if (appointmentSnapshot.exists()) {
                    String date = parseStringValue(appointmentSnapshot.child("date").getValue(), appointmentId, "date");
                    String time = parseStringValue(appointmentSnapshot.child("time").getValue(), appointmentId, "time");
                    Long doctorIdLong = getLongValue(appointmentSnapshot.child("doctorId"), appointmentId, "doctorId");
                    String formattedDate = (date != null ? date : "Unknown Date");
                    String selectedTime = (time != null ? time : "Unknown Time");
                    String doctorId = doctorIdLong != null ? String.valueOf(doctorIdLong) : null;

                    if (doctorId == null) {
                        FirebaseErrorHandler.handleError(null, null, "Doctor ID not found for appointment ID: " + appointmentId);
                        new Thread(() -> result.postValue(false)).start();
                        return;
                    }

                    DatabaseReference doctorRef = firebaseDatabase.getReference(Constants.DB_PATH_DOCTORS).child(doctorId);
                    doctorRef.get().addOnCompleteListener(doctorTask -> {
                        if (doctorTask.isSuccessful()) {
                            DataSnapshot doctorSnapshot = doctorTask.getResult();
                            final String doctorName = doctorSnapshot.exists() && doctorSnapshot.child("Name").getValue(String.class) != null ?
                                    doctorSnapshot.child("Name").getValue(String.class) : "Unknown Doctor";

                            notificationsRef.orderByChild("appointmentId").equalTo(appointmentId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                                                    String notificationId = notificationSnapshot.getKey();
                                                    if (notificationId != null) {
                                                        Map<String, Object> updates = new HashMap<>();
                                                        String currentTime = LocalDateTime.now()
                                                                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.US));
                                                        updates.put("createdAt", currentTime);
                                                        updates.put("isRead", false);
                                                        updates.put("message", "Appointment with " + doctorName + " on " +
                                                                formattedDate + " " + selectedTime + " (" + AppointmentStatus.CANCELED + ")");

                                                        notificationsRef.child(notificationId).updateChildren(updates)
                                                                .addOnSuccessListener(aVoid -> new Thread(() -> result.postValue(true)).start())
                                                                .addOnFailureListener(e -> {
                                                                    new Thread(() -> result.postValue(false)).start();
                                                                });
                                                    }
                                                }
                                            } else {
                                                FirebaseErrorHandler.handleError(null, null, "No notification found for appointment ID: " + appointmentId);
                                                new Thread(() -> result.postValue(false)).start();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            new Thread(() -> result.postValue(false)).start();
                                        }
                                    });
                        } else {
                            new Thread(() -> result.postValue(false)).start();
                        }
                    });
                } else {
                    new Thread(() -> result.postValue(false)).start();
                }
            } else {
                new Thread(() -> result.postValue(false)).start();
            }
        });

        return result;
    }

    public LiveData<Boolean> deleteAppointmentNotes(String userId, String appointmentId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        DatabaseReference appointmentRef = firebaseDatabase.getReference(Constants.DB_PATH_APPOINTMENTS)
                .child(userId).child(appointmentId);

        appointmentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    appointmentRef.child("notes").removeValue()
                            .addOnSuccessListener(aVoid -> new Thread(() -> result.postValue(true)).start())
                            .addOnFailureListener(e -> {
                                new Thread(() -> result.postValue(false)).start();
                            });
                } else {
                    FirebaseErrorHandler.handleError(null, null, "Appointment not found for ID: " + appointmentId);
                    new Thread(() -> result.postValue(false)).start();
                }
            } else {
                new Thread(() -> result.postValue(false)).start();
            }
        });

        return result;
    }

    public LiveData<String> checkAndUpdatePassword(String userId, String currentPassword, String newPassword) {
        MutableLiveData<String> result = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_USERS).child(userId).child("password");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storedPass = snapshot.getValue(String.class);
                if (storedPass != null && storedPass.equals(currentPassword)) {
                    ref.setValue(newPassword)
                            .addOnSuccessListener(aVoid -> result.setValue("Success"))
                            .addOnFailureListener(e -> result.setValue("Failed to update password: " + e.getMessage()));
                } else {
                    result.setValue("Current password is incorrect");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseErrorHandler.handleError(null, error, "Failed to check password");
                result.setValue("Failed to check password: " + error.getMessage());
            }
        });
        return result;
    }

    public LiveData<Boolean> checkFavorite(String userId, int doctorId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        firebaseDatabase.getReference(Constants.DB_PATH_FAVORITES).child(userId).child(String.valueOf(doctorId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        result.setValue(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.setValue(false);
                    }
                });
        return result;
    }

    public LiveData<Boolean> toggleFavorite(String userId, int doctorId, boolean isFavorite) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        DatabaseReference ref = firebaseDatabase.getReference(Constants.DB_PATH_FAVORITES).child(userId).child(String.valueOf(doctorId));
        if (isFavorite) {
            ref.removeValue().addOnCompleteListener(task -> result.setValue(task.isSuccessful()));
        } else {
            ref.setValue(true).addOnCompleteListener(task -> result.setValue(task.isSuccessful()));
        }
        return result;
    }

    public LiveData<List<DoctorsModel>> getFavoriteDoctors(String userId, List<DoctorsModel> allDoctors) {
        MutableLiveData<List<DoctorsModel>> result = new MutableLiveData<>();
        firebaseDatabase.getReference(Constants.DB_PATH_FAVORITES).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> favoriteIds = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        favoriteIds.add(Integer.parseInt(child.getKey()));
                    } catch (NumberFormatException ignored) {}
                }
                List<DoctorsModel> favoriteDoctors = new ArrayList<>();
                for (DoctorsModel doctor : allDoctors) {
                    if (favoriteIds.contains(doctor.getId())) {
                        favoriteDoctors.add(doctor);
                    }
                }
                result.setValue(favoriteDoctors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.setValue(new ArrayList<>());
            }
        });
        return result;
    }

    private Long getLongValue(DataSnapshot snapshot, String appointmentId, String fieldName) {
        if (!snapshot.exists()) {
            return null;
        }
        try {
            Object value = snapshot.getValue();
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String parseStringValue(Object value, String appointmentId, String fieldName) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Long || value instanceof Integer) {
            long numValue = value instanceof Long ? (Long) value : (Integer) value;
            return String.valueOf(numValue);
        }
        return null;
    }

    private String parseStringOrNumber(Object value, String appointmentId, String fieldName) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Long || value instanceof Integer || value instanceof Double) {
            long numValue = value instanceof Long ? (Long) value :
                    value instanceof Integer ? (Integer) value : ((Double) value).longValue();
            return String.valueOf(numValue);
        }
        return null;
    }

    private String parseStatusValue(Object statusObj, String appointmentId) {
        if (statusObj instanceof String) {
            return ((String) statusObj).toUpperCase(Locale.US);
        } else if (statusObj instanceof Long || statusObj instanceof Integer) {
            long numValue = statusObj instanceof Long ? (Long) statusObj : (Integer) statusObj;
            switch ((int) numValue) {
                case 1:
                    return "UPCOMING";
                case 2:
                    return "COMPLETED";
                case 3:
                    return "CANCELED";
                default:
                    return null;
            }
        }
        return null;
    }
}