package com.example.doctorapp.Domain;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

public class NotificationModel implements Serializable {
    private String id;
    private String message;
    private String createdAt;
    private String appointmentId;
    @PropertyName("isRead")
    private boolean isRead;

    public NotificationModel() {}

    public NotificationModel(String id, String message, String createdAt, boolean isRead, String appointmentId) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.appointmentId = appointmentId;
    }

    public NotificationModel(String message, String createdAt, boolean isRead, String appointmentId) {
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.appointmentId = appointmentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    @PropertyName("isRead")
    public boolean isRead() { return isRead; }
    @PropertyName("isRead")
    public void setRead(boolean read) { isRead = read; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
}