package com.example.doctorapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.doctorapp.Adapter.TopDoctorsAdapter2;
import com.example.doctorapp.Utils.Constants;
import com.example.doctorapp.Domain.DoctorsModel;
import com.example.doctorapp.Utils.FirebaseErrorHandler;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityTopDoctorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopDoctorsActivity extends BaseActivity {
    private ActivityTopDoctorsBinding binding;
    private TopDoctorsAdapter2 adapter;
    private List<DoctorsModel> doctorList = new ArrayList<>();
    private List<DoctorsModel> allDoctors = new ArrayList<>();
    private boolean isShowingFavorites = false;
    private SessionManager sessionManager;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTopDoctorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupRecyclerView();
        loadDoctors();
        binding.backBtn.setOnClickListener(v -> finish());
        if (binding.isFav != null) {
            binding.isFav.setOnClickListener(v -> toggleFavoriteFilter());
        } else {
            Log.w("TopDoctorsActivity", "isFavBtn is null, check activity_top_doctors.xml");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.getUserUid().equals("Guest")) {
            if (isShowingFavorites) {
                filterFavoriteDoctors();
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new TopDoctorsAdapter2(this, doctorList, this);
        binding.viewTopDoctorList.setLayoutManager(new LinearLayoutManager(this));
        binding.viewTopDoctorList.setAdapter(adapter);
    }

    private void loadDoctors() {
        binding.progressBarTopDoctor.setVisibility(View.VISIBLE);
        viewModel.loadDoctors().observe(this, doctors -> {
            Log.d("TopDoctorsActivity", "loadDoctors: size=" + doctors.size());
            allDoctors.clear();
            allDoctors.addAll(doctors);
            doctorList.clear();
            doctorList.addAll(doctors);
            if (isShowingFavorites) {
                filterFavoriteDoctors();
            } else {
                adapter.updateList(doctorList);
            }
            binding.progressBarTopDoctor.setVisibility(View.GONE);
            if (doctorList.isEmpty()) {
                Toast.makeText(this, "No doctors available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavoriteFilter() {
        if (sessionManager.getUserUid().equals("Guest")) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }
        isShowingFavorites = !isShowingFavorites;
        binding.isFav.setText(isShowingFavorites ? "Show All Doctors" : "Show Favorites");
        if (isShowingFavorites) {
            filterFavoriteDoctors();
        } else {
            doctorList.clear();
            doctorList.addAll(allDoctors);
            adapter.updateList(doctorList);
        }
    }

    private void filterFavoriteDoctors() {
        binding.progressBarTopDoctor.setVisibility(View.VISIBLE);
        viewModel.getFavoriteDoctors(sessionManager.getUserUid(), allDoctors).observe(this, favorites -> {
            Log.d("TopDoctorsActivity", "filterFavoriteDoctors: size=" + favorites.size());
            if (isShowingFavorites) {
                doctorList.clear();
                doctorList.addAll(favorites);
                adapter.updateList(doctorList);
                if (favorites.isEmpty()) {
                    Toast.makeText(this, "No favorite doctors found", Toast.LENGTH_SHORT).show();
                }
            }
            binding.progressBarTopDoctor.setVisibility(View.GONE);
        });
    }
}