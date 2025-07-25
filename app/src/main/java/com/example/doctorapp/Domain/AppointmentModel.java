package com.example.doctorapp.Domain;

import java.io.Serializable;

public class AppointmentModel implements Serializable {
    private String appointmentId, date, time, reason, createdAt, notes;
    private int doctorId;
    private AppointmentStatus status;

    public AppointmentModel() {}

    public AppointmentModel(int doctorId, String date, String time, AppointmentStatus status, String reason, String createdAt, String notes) {
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.notes = notes;
    }

    public AppointmentModel(String appointmentId, int doctorId, String date, String time, AppointmentStatus status, String reason, String createdAt, String notes) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.notes = notes;
    }

    public String getAppointmentId() { return appointmentId; }
    public int getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getCreatedAt() { return createdAt; }
    public String getNotes() { return notes; }

    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setNotes(String notes) { this.notes = notes; }
}