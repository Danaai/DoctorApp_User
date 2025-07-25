package com.example.doctorapp.Adapter;

import static com.example.doctorapp.Domain.AppointmentStatus.CANCELED;
import static com.example.doctorapp.Domain.AppointmentStatus.COMPLETED;
import static com.example.doctorapp.Domain.AppointmentStatus.UPCOMING;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Activity.AppointmentDetailActivity;
import com.example.doctorapp.Domain.AppointmentModel;
import com.example.doctorapp.Domain.AppointmentStatus;
import com.example.doctorapp.R;
import com.example.doctorapp.databinding.ViewholderAppointmentBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final Context context;
    private final List<AppointmentModel> appointments;
    private final Map<String, String> doctorNames;
    private final Map<String, String> doctorSpecializations;
    private final Map<String, String> doctorImages;

    public AppointmentAdapter(Context context, List<AppointmentModel> appointments,
                              Map<String, String> doctorNames, Map<String, String> doctorSpecializations,
                              Map<String, String> doctorImages) {
        this.context = context;
        this.appointments = new ArrayList<>(appointments);
        this.doctorNames = doctorNames;
        this.doctorSpecializations = doctorSpecializations;
        this.doctorImages = doctorImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAppointmentBinding binding = ViewholderAppointmentBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentModel appointment = appointments.get(position);
        String doctorId = String.valueOf(appointment.getDoctorId());

        holder.binding.doctorNameTxt.setText(doctorNames.getOrDefault(doctorId, "Unknown"));
        holder.binding.specialTxt.setText(doctorSpecializations.getOrDefault(doctorId, ""));
        holder.binding.dateTxt.setText(appointment.getDate() != null ? "Date: " + appointment.getDate() : "N/A");
        holder.binding.timeTxt.setText(appointment.getTime() != null ? "Time: " + appointment.getTime() : "N/A");
        holder.binding.reasonTxt.setText(appointment.getReason() != null ? "Reason: " + appointment.getReason() : "N/A");
        holder.binding.createdAtTxt.setText(appointment.getCreatedAt() != null ? "Created At: " + appointment.getCreatedAt() : "N/A");

        if (appointment.getStatus() != null) {
            holder.binding.statusTxt.setText("⬤ " + appointment.getStatus().toString());
            switch (appointment.getStatus()) {
                case UPCOMING:
                    holder.binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.status_upcoming));
                    break;
                case CANCELED:
                    holder.binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.red));
                    break;
                case COMPLETED:
                    holder.binding.statusTxt.setTextColor(ContextCompat.getColor(context, R.color.status_complete));
                    break;
            }
        } else {
            holder.binding.statusTxt.setText("Unknown");
        }

        Glide.with(context)
                .load(doctorImages.getOrDefault(doctorId, ""))
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.profile))
                .into(holder.binding.img);

        holder.itemView.setOnClickListener(v -> {
            if (appointment.getStatus() == null || appointment.getAppointmentId() == null) {
                Log.e("AppointmentAdapter", "Cannot open detail: Invalid appointment data for ID: " + appointment.getAppointmentId());
                Toast.makeText(context, "Invalid appointment data", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, AppointmentDetailActivity.class);
            intent.putExtra("appointment", appointment);
            intent.putExtra("userName", doctorNames.getOrDefault(doctorId, "Unknown"));
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("AppointmentAdapter", "Error starting AppointmentDetailActivity: " + e.getMessage());
                Toast.makeText(context, "Failed to open appointment details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.binding.img);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void updateList(List<AppointmentModel> newList) {
        List<AppointmentModel> newListCopy = new ArrayList<>(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return appointments.size(); }
            @Override
            public int getNewListSize() { return newListCopy.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return appointments.get(oldPos).getAppointmentId().equals(newListCopy.get(newPos).getAppointmentId());
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return appointments.get(oldPos).equals(newListCopy.get(newPos));
            }
        });
        appointments.clear();
        appointments.addAll(newListCopy);
        diffResult.dispatchUpdatesTo(this);
    }

    public Map<String, String> getNameMap() {
        return doctorNames;
    }

    public Map<String, String> getSpecializationMap() {
        return doctorSpecializations;
    }

    public Map<String, String> getImageMap() {
        return doctorImages;
    }

    public List<AppointmentModel> getCurrentList() {
        return new ArrayList<>(appointments);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderAppointmentBinding binding;

        public ViewHolder(ViewholderAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}