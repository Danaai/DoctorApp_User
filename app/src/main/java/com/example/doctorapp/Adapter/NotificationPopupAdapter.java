package com.example.doctorapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.Activity.AppointmentDetailActivity;
import com.example.doctorapp.Activity.NotificationActivity;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.Domain.NotificationModel;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationPopupAdapter extends RecyclerView.Adapter<NotificationPopupAdapter.ViewHolder> {
    private List<NotificationModel> notificationModels;
    private String userId;
    private Context context;
    private DatabaseReference notiRef;

    public NotificationPopupAdapter(String userId, Context context) {
        this.notificationModels = new ArrayList<>();
        this.userId = userId;
        this.context = context;
        this.notiRef = FirebaseDatabase.getInstance().getReference(Constants.DB_PATH_NOTIFICATIONS).child(userId);
    }

    public void setNotifications(List<NotificationModel> items) {
        this.notificationModels = items != null ? items : new ArrayList<>();

        Collections.sort(notificationModels, new Comparator<NotificationModel>() {
            @Override
            public int compare(NotificationModel o1, NotificationModel o2) {
                if (o1.isRead() && !o2.isRead()) return 1;
                if (!o1.isRead() && o2.isRead()) return -1;

                SimpleDateFormat[] formats = {
                        new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                };

                Date date1 = null, date2 = null;
                String createdAt1 = o1.getCreatedAt();
                String createdAt2 = o2.getCreatedAt();

                for (SimpleDateFormat sdf : formats) {
                    try {
                        if (date1 == null && createdAt1 != null) {
                            date1 = sdf.parse(createdAt1);
                        }
                        if (date2 == null && createdAt2 != null) {
                            date2 = sdf.parse(createdAt2);
                        }
                    } catch (ParseException e) {
                        Log.w("NotificationAdapter", "Failed to parse date with format " + sdf.toPattern() + ": " + e.getMessage());
                    }
                }

                if (date1 != null && date2 != null) {
                    return date2.compareTo(date1);
                } else {
                    Log.e("NotificationAdapter", "Could not parse dates: createdAt1=" + createdAt1 + ", createdAt2=" + createdAt2);
                    return 0;
                }
            }
        });

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel item = notificationModels.get(position);

        // Kiểm tra null cho các view
        if (holder.messageTxt == null || holder.timeTxt == null || holder.unreadIndicator == null || holder.typeTxt == null || holder.notesTxt == null) {
            Log.e("NotificationAdapter", "One or more views are null at position " + position +
                    ": messageTxt=" + (holder.messageTxt != null ? "not null" : "null") +
                    ", timeTxt=" + (holder.timeTxt != null ? "not null" : "null") +
                    ", unreadIndicator=" + (holder.unreadIndicator != null ? "not null" : "null") +
                    ", typeTxt=" + (holder.typeTxt != null ? "not null" : "null") +
                    ", notesTxt=" + (holder.notesTxt != null ? "not null" : "null"));
            return;
        }

        // Gán dữ liệu với kiểm tra null
        holder.messageTxt.setText(item.getMessage() != null ? item.getMessage() : "No message");
        holder.timeTxt.setText(item.getCreatedAt() != null ? "Created At: " + item.getCreatedAt() : "No time");
        holder.unreadIndicator.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

        // Xử lý status từ Firebase
        DatabaseReference apptRef = FirebaseDatabase.getInstance().getReference("Appointments")
                .child(userId).child(item.getAppointmentId());
        apptRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String displayStatus = (status != null ? status : "UNKNOWN").toUpperCase();
                    holder.typeTxt.setText(displayStatus);

                    switch (displayStatus) {
                        case "UPCOMING":
                            holder.typeTxt.setBackgroundResource(R.drawable.status_upcoming_bg);
                            break;
                        case "CANCELED":
                            holder.typeTxt.setBackgroundResource(R.drawable.status_cancelled_bg);
                            break;
                        case "COMPLETED":
                            holder.typeTxt.setBackgroundResource(R.drawable.status_confirmed_bg);
                            break;
                        default:
                            holder.typeTxt.setBackgroundResource(R.drawable.blue_btn_bg);
                            break;
                    }
                } else {
                    holder.typeTxt.setText("UNKNOWN");
                    holder.typeTxt.setBackgroundResource(R.drawable.blue_btn_bg);
                    Log.e("NotificationAdapter", "Appointment not found for ID: " + item.getAppointmentId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationAdapter", "Error fetching status: " + error.getMessage());
                holder.typeTxt.setText("UNKNOWN");
                holder.typeTxt.setBackgroundResource(R.drawable.blue_btn_bg);
            }
        });

        // Xử lý notes cho NotificationActivity
        if (context instanceof NotificationActivity) {
            apptRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String notes = snapshot.child("notes").getValue(String.class);
                    Log.d("NotificationAdapter", "Fetching notes for AppointmentId: " + item.getAppointmentId() + ", Notes: " + notes);
                    if (notes != null && !notes.isEmpty()) {
                        holder.notesTxt.setText("Notes: " + notes);
                        holder.notesTxt.setVisibility(View.VISIBLE);
                    } else {
                        holder.notesTxt.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("NotificationAdapter", "Error fetching notes: " + error.getMessage());
                    holder.notesTxt.setVisibility(View.GONE);
                }
            });
        } else {
            holder.notesTxt.setVisibility(View.GONE);
        }

        // Xử lý sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            notiRef.child(item.getId()).child("isRead").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        item.setRead(true);
                        holder.unreadIndicator.setVisibility(View.GONE);
                        notifyDataSetChanged();
                        Log.d("NotificationAdapter", "Marked as read: " + item.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationAdapter", "Failed to mark as read: " + e.getMessage());
                    });

            if (item.getAppointmentId() != null) {
                apptRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String appointmentId = snapshot.getKey();
                            Long doctorIdLong = snapshot.child("doctorId").getValue(Long.class);
                            int doctorId = (doctorIdLong != null) ? doctorIdLong.intValue() : -1;
                            String date = snapshot.child("date").getValue(String.class);
                            String time = snapshot.child("time").getValue(String.class);
                            String reason = snapshot.child("reason").getValue(String.class);
                            String status = snapshot.child("status").getValue(String.class);
                            String createdAt = snapshot.child("createdAt").getValue(String.class);
                            String notes = snapshot.child("notes").getValue(String.class);

                            AppointmentStatus appointmentStatus;
                            try {
                                appointmentStatus = AppointmentStatus.valueOf(status != null ? status.toUpperCase() : "UNKNOWN");
                            } catch (IllegalArgumentException e) {
                                appointmentStatus = AppointmentStatus.UNKNOWN;
                                Log.e("NotificationAdapter", "Invalid status value: " + status);
                            }

                            AppointmentModel appointment = new AppointmentModel(appointmentId, doctorId, date, time, appointmentStatus, reason, createdAt, notes);

                            Intent intent = new Intent(context, AppointmentDetailActivity.class);
                            intent.putExtra("appointment", appointment);
                            intent.putExtra("userId", userId);
                            context.startActivity(intent);
                        } else {
                            Log.e("NotificationAdapter", "Appointment not found for ID: " + item.getAppointmentId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationAdapter", "Error fetching appointment: " + error.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTxt, messageTxt, timeTxt, notesTxt;
        ImageView unreadIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            try {
                typeTxt = itemView.findViewById(R.id.notification_type);
                messageTxt = itemView.findViewById(R.id.notification_message);
                timeTxt = itemView.findViewById(R.id.notification_time);
                notesTxt = itemView.findViewById(R.id.notification_notes);
                unreadIndicator = itemView.findViewById(R.id.unread_indicator);
                Log.d("NotificationAdapter", "ViewHolder initialized - typeTxt: " + (typeTxt != null ? "not null" : "null"));
                Log.d("NotificationAdapter", "ViewHolder initialized - messageTxt: " + (messageTxt != null ? "not null" : "null"));
                Log.d("NotificationAdapter", "ViewHolder initialized - timeTxt: " + (timeTxt != null ? "not null" : "null"));
                Log.d("NotificationAdapter", "ViewHolder initialized - notesTxt: " + (notesTxt != null ? "not null" : "null"));
                Log.d("NotificationAdapter", "ViewHolder initialized - unreadIndicator: " + (unreadIndicator != null ? "not null" : "null"));
            } catch (Exception e) {
                Log.e("NotificationAdapter", "Error initializing ViewHolder: " + e.getMessage());
            }
        }
    }
}