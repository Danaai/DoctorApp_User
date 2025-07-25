package com.example.doctorapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Activity.DetailActivity;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.databinding.ViewholderTopDoctorBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TopDoctorsAdapter extends RecyclerView.Adapter<TopDoctorsAdapter.ViewHolder> {
    private final List<DoctorsModel> doctors;
    private Context context;

    public TopDoctorsAdapter(Context context, List<DoctorsModel> doctors) {
        this.context = context;
        this.doctors = doctors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTopDoctorBinding binding = ViewholderTopDoctorBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorsModel doctor = doctors.get(position);
        holder.binding.nameTxt.setText(doctor.getName() != null ? doctor.getName() : "N/A");
        holder.binding.ratingTxt.setText(String.valueOf(doctor.getRating()));
        holder.binding.patientsTxt.setText(doctor.getPatients() + " Years");

        int specialId = doctor.getSpecial();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance()
                .getReference("Category").child(String.valueOf(specialId)).child("Name");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.binding.specialTxt.setText(snapshot.exists() ? snapshot.getValue(String.class) : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.specialTxt.setText("Error");
            }
        });

        Glide.with(context)
                .load(doctor.getPicture())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.img);

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", doctor);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public void updateList(List<DoctorsModel> newList) {
        doctors.clear();
        doctors.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTopDoctorBinding binding;

        public ViewHolder(ViewholderTopDoctorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}