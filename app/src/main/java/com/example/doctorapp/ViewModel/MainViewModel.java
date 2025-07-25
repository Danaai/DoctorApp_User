package com.example.doctorapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.Domain.CategoryModel;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.Domain.UserModel;
import com.example.doctorapp.Repository.MainRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainViewModel extends ViewModel {
    private final MainRepository repository;
    private final MediatorLiveData<List<AppointmentModel>> sortedAppointments = new MediatorLiveData<>();
    private final MediatorLiveData<Map<String, String>> doctorNameMap = new MediatorLiveData<>();
    private final MediatorLiveData<Map<String, String>> doctorSpecializationMap = new MediatorLiveData<>();
    private final MediatorLiveData<Map<String, String>> doctorImageMap = new MediatorLiveData<>();
    private final MediatorLiveData<List<LanguageModel>> languages = new MediatorLiveData<>();
    private final MediatorLiveData<UserModel> userData = new MediatorLiveData<>();

    public MainViewModel() {
        this.repository = new MainRepository();
    }

    public void loadAppointments(String userId) {
        LiveData<List<AppointmentModel>> appointmentsLiveData = repository.loadAppointments(userId);
        sortedAppointments.addSource(appointmentsLiveData, appointments -> {
            if (appointments != null) {
                sortedAppointments.setValue(sortAppointments(new ArrayList<>(appointments)));
            } else {
                sortedAppointments.setValue(new ArrayList<>());
            }
        });
    }

    public void loadDoctorMaps() {
        LiveData<Map<String, Object>> doctorMapsLiveData = repository.loadDoctorMaps();
        doctorMapsLiveData.observeForever(doctorMaps -> {
            if (doctorMaps != null) {
                doctorNameMap.setValue((Map<String, String>) doctorMaps.get("nameMap"));
                doctorSpecializationMap.setValue((Map<String, String>) doctorMaps.get("specializationMap"));
                doctorImageMap.setValue((Map<String, String>) doctorMaps.get("imageMap"));
            } else {
                doctorNameMap.setValue(new HashMap<>());
                doctorSpecializationMap.setValue(new HashMap<>());
                doctorImageMap.setValue(new HashMap<>());
            }
        });
    }

    public void loadLanguages() {
        LiveData<List<LanguageModel>> languagesLiveData = repository.loadLanguages();
        languages.addSource(languagesLiveData, languageList -> {
            if (languageList != null) {
                languages.setValue(new ArrayList<>(languageList));
            } else {
                languages.setValue(new ArrayList<>());
            }
        });
    }

    public void loadUserData(String userId) {
        LiveData<UserModel> userLiveData = repository.loadUserData(userId);
        userData.addSource(userLiveData, user -> userData.setValue(user));
    }

    public LiveData<UserModel> getUserData() {
        return userData;
    }

    public LiveData<Boolean> updateUserData(String userId, UserModel user) {
        return repository.updateUserData(userId, user);
    }

    public LiveData<Boolean> cancelAppointmentAndUpdateNotification(String userId, String appointmentId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        LiveData<Boolean> updateStatusLiveData = repository.updateAppointmentStatus(userId, appointmentId, AppointmentStatus.CANCELED);

        updateStatusLiveData.observeForever(statusSuccess -> {
            if (statusSuccess != null && statusSuccess) {
                repository.updateNotification(userId, appointmentId).observeForever(notificationSuccess -> {
                    if (notificationSuccess != null && notificationSuccess) {
                        repository.deleteAppointmentNotes(userId, appointmentId).observeForever(notesSuccess -> {
                            if (notesSuccess != null && notesSuccess) {
                                result.setValue(true);
                            } else {
                                result.setValue(false);
                            }
                        });
                    } else {
                        result.setValue(false);
                    }
                });
            } else {
                result.setValue(false);
            }
        });

        return result;
    }

    public LiveData<String> checkAndUpdatePassword(String userId, String currentPassword, String newPassword) {
        return repository.checkAndUpdatePassword(userId, currentPassword, newPassword);
    }

    public LiveData<List<AppointmentModel>> getSortedAppointments() {
        return sortedAppointments;
    }

    public LiveData<Map<String, String>> getDoctorNameMap() {
        return doctorNameMap;
    }

    public LiveData<Map<String, String>> getDoctorSpecializationMap() {
        return doctorSpecializationMap;
    }

    public LiveData<Map<String, String>> getDoctorImageMap() {
        return doctorImageMap;
    }

    public LiveData<List<LanguageModel>> getLanguages() {
        return languages;
    }

    public LiveData<List<CategoryModel>> loadCategories() {
        return repository.loadCategories();
    }

    public LiveData<List<DoctorsModel>> loadDoctors() {
        return repository.loadDoctors();
    }

    public LiveData<Map<Integer, String>> loadCategoryNames() {
        return repository.loadCategoryNames();
    }

    public LiveData<Boolean> checkFavorite(String userId, int doctorId) {
        return repository.checkFavorite(userId, doctorId);
    }

    public LiveData<Boolean> toggleFavorite(String userId, int doctorId, boolean isFavorite) {
        return repository.toggleFavorite(userId, doctorId, isFavorite);
    }

    public LiveData<List<DoctorsModel>> getFavoriteDoctors(String userId, List<DoctorsModel> allDoctors) {
        return repository.getFavoriteDoctors(userId, allDoctors);
    }

    private List<AppointmentModel> sortAppointments(List<AppointmentModel> appointments) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a", Locale.US);

        List<AppointmentModel> validAppointments = new ArrayList<>();
        for (AppointmentModel appointment : appointments) {
            try {
                String dateTimeStr = appointment.getDate() + " " + appointment.getTime();
                LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
                validAppointments.add(appointment);
            } catch (DateTimeParseException e) {
                // Skip invalid appointments
            }
        }

        Collections.sort(validAppointments, new Comparator<AppointmentModel>() {
            @Override
            public int compare(AppointmentModel a1, AppointmentModel a2) {
                int statusPriority1 = getStatusPriority(a1.getStatus());
                int statusPriority2 = getStatusPriority(a2.getStatus());

                if (statusPriority1 != statusPriority2) {
                    return statusPriority2 - statusPriority1;
                }

                try {
                    String dateTime1Str = a1.getDate() + " " + a1.getTime();
                    String dateTime2Str = a2.getDate() + " " + a2.getTime();

                    LocalDateTime dateTime1 = LocalDateTime.parse(dateTime1Str, dateTimeFormatter);
                    LocalDateTime dateTime2 = LocalDateTime.parse(dateTime2Str, dateTimeFormatter);

                    if ("UPCOMING".equalsIgnoreCase(a1.getStatus().toString())) {
                        return dateTime1.compareTo(dateTime2);
                    } else {
                        return dateTime2.compareTo(dateTime1);
                    }
                } catch (DateTimeParseException e) {
                    return 0;
                }
            }
        });

        return validAppointments;
    }

    private int getStatusPriority(AppointmentStatus status) {
        if (status == null) return 0;
        switch (status) {
            case UPCOMING:
                return 3;
            case COMPLETED:
                return 2;
            case CANCELED:
                return 1;
            default:
                return 0;
        }
    }
}