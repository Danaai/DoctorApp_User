package com.example.doctorapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.doctorapp.Adapter.LanguageAdapter;
import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.Utils.SessionManager;
import com.example.doctorapp.ViewModel.MainViewModel;
import com.example.doctorapp.databinding.ActivityLanguageBinding;

import java.util.ArrayList;

public class LanguageActivity extends BaseActivity {
    private ActivityLanguageBinding binding;
    private LanguageAdapter languageAdapter;
    private SessionManager sessionManager;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupRecyclerView();
        observeLanguages();
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        languageAdapter = new LanguageAdapter(new ArrayList<>(), this, position -> {
            LanguageModel selectedLanguage = languageAdapter.getCurrentList().get(position);
            sessionManager.saveUserSession(
                    sessionManager.getUserUid(),
                    sessionManager.getUserId(),
                    selectedLanguage.getId()
            );
            Toast.makeText(this, "Selected: " + selectedLanguage.getName(), Toast.LENGTH_SHORT).show();
        });
        binding.lagView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.lagView.setAdapter(languageAdapter);
    }

    private void observeLanguages() {
        // Giả định có ProgressBar với ID progressBar
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        viewModel.loadLanguages();
        viewModel.getLanguages().observe(this, languages -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            if (languages == null || languages.isEmpty()) {
                Toast.makeText(this, "No languages available", Toast.LENGTH_SHORT).show();
                languageAdapter.updateList(new ArrayList<>());
            } else {
                languageAdapter.updateList(languages);
            }
        });
    }
}