package com.example.doctorapp.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.doctorapp.Adapter.AppointmentAdapter;
import com.example.doctorapp.R;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityAppointmentsBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentsActivity extends BaseActivity {
    private ActivityAppointmentsBinding binding;
    private AppointmentAdapter adapter;
    private SessionManager sessionManager;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAppointmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserUid();

        if (userId.equals("Guest")) {
            Toast.makeText(this, "Please log in to view appointments!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (bottomNavigationBinding != null) {
            bottomNavigationBinding.appointmentImg.setColorFilter(Color.WHITE);
            bottomNavigationBinding.navAppointment.setBackgroundColor(
                    getResources().getColor(R.color.blue));
        }

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupRecyclerView();
        observeData(userId);
    }

    private void setupRecyclerView() {
        if (binding.appointmentsRecyclerView == null) {
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(this, new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        binding.appointmentsRecyclerView.setAdapter(adapter);
    }

    private void observeData(String userId) {
        binding.progressBarTopDoctor.setVisibility(View.VISIBLE);

        // Observe doctor maps
        viewModel.loadDoctorMaps();
        viewModel.getDoctorNameMap().observe(this, nameMap -> {
            if (nameMap != null) {
                adapter = new AppointmentAdapter(
                        this,
                        adapter.getCurrentList(),
                        nameMap,
                        adapter.getSpecializationMap(),
                        adapter.getImageMap()
                );
                binding.appointmentsRecyclerView.setAdapter(adapter);
            }
        });

        viewModel.getDoctorSpecializationMap().observe(this, specializationMap -> {
            if (specializationMap != null) {
                adapter = new AppointmentAdapter(
                        this,
                        adapter.getCurrentList(),
                        adapter.getNameMap(),
                        specializationMap,
                        adapter.getImageMap()
                );
                binding.appointmentsRecyclerView.setAdapter(adapter);
            }
        });

        viewModel.getDoctorImageMap().observe(this, imageMap -> {
            if (imageMap != null) {
                adapter = new AppointmentAdapter(
                        this,
                        adapter.getCurrentList(),
                        adapter.getNameMap(),
                        adapter.getSpecializationMap(),
                        imageMap
                );
                binding.appointmentsRecyclerView.setAdapter(adapter);
            }
        });

        // Observe appointments
        viewModel.loadAppointments(userId);
        viewModel.getSortedAppointments().observe(this, appointments -> {
            binding.progressBarTopDoctor.setVisibility(View.GONE);
            if (appointments == null || appointments.isEmpty()) {
                Toast.makeText(this, "No valid appointments found", Toast.LENGTH_SHORT).show();
                adapter.updateList(new ArrayList<>());
            } else {
                adapter.updateList(appointments);
            }
        });
    }
}