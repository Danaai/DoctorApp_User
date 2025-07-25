package com.example.doctorapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Activity.DetailActivity;
import com.example.doctorapp.Activity.LoginActivity;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.R;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ViewholderTopDoctor2Binding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TopDoctorsAdapter2 extends RecyclerView.Adapter<TopDoctorsAdapter2.ViewHolder> {
    private final List<DoctorsModel> doctors;
    private Context context;
    private MainViewModel viewModel;
    private SessionManager sessionManager;
    private LifecycleOwner lifecycleOwner;

    public TopDoctorsAdapter2(Context context, List<DoctorsModel> doctors, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.doctors = new ArrayList<>(doctors);
        this.viewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(MainViewModel.class);
        this.sessionManager = new SessionManager(context);
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTopDoctor2Binding binding = ViewholderTopDoctor2Binding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorsModel doctor = doctors.get(position);
        String uid = sessionManager.getUserUid();

        holder.binding.nameTxt.setText(doctor.getName() != null ? doctor.getName() : "N/A");
        holder.binding.ratingTxt.setText(String.valueOf(doctor.getRating()));
        holder.binding.ratingBar.setRating((float) doctor.getRating());
        holder.binding.degreeTxt.setText("Professional Doctor");

        int specialId = doctor.getSpecial();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance()
                .getReference(Constants.DB_PATH_CATEGORIES).child(String.valueOf(specialId)).child("Name");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.binding.specialTxt.setText(snapshot.exists() ? snapshot.getValue(String.class) : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.specialTxt.setText("Error");
                FirebaseErrorHandler.handleError(context, error, "Failed to load category");
            }
        });

        Glide.with(context)
                .load(doctor.getPicture())
                .apply(new RequestOptions()
                        .transform(new CenterCrop())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.profile))
                .into(holder.binding.img);

        if (!uid.equals("Guest")) {
            holder.binding.favBtn.setVisibility(View.VISIBLE);
            viewModel.checkFavorite(uid, doctor.getId()).observe(lifecycleOwner, isFavorite -> {
                holder.binding.favBtn.setImageResource(isFavorite ? R.drawable.favorite_black : R.drawable.favorite_white);
            });

            // Handle favorite click
            holder.binding.favBtn.setOnClickListener(v -> {
                if (uid.equals("Guest")) {
                    Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, LoginActivity.class));
                    return;
                }
                holder.binding.favBtn.setEnabled(false);
                viewModel.checkFavorite(uid, doctor.getId()).observe(lifecycleOwner, isFavorite -> {
                    viewModel.toggleFavorite(uid, doctor.getId(), isFavorite).observe(lifecycleOwner, success -> {
                        holder.binding.favBtn.setEnabled(true);
                        if (success) {
                            holder.binding.favBtn.setImageResource(!isFavorite ? R.drawable.favorite_black : R.drawable.favorite_white);
                            Toast.makeText(context, !isFavorite ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(context, "Failed to update favorite", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        } else {
            holder.binding.favBtn.setVisibility(View.GONE);
        }

        holder.binding.viewBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", doctor);
            context.startActivity(intent);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.binding.img);
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public void updateList(List<DoctorsModel> newList) {
        doctors.clear();
        doctors.addAll(new ArrayList<>(newList));
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTopDoctor2Binding binding;

        public ViewHolder(ViewholderTopDoctor2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}