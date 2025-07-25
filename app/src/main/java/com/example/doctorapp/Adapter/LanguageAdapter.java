package com.example.doctorapp.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Domain.LanguageModel;
import com.example.doctorapp.R;
import com.example.doctorapp.databinding.ViewholderLanguageBinding;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private final List<LanguageModel> languages;
    private final Context context;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LanguageAdapter(List<LanguageModel> languages, Context context, OnItemClickListener listener) {
        this.languages = new ArrayList<>(languages);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderLanguageBinding binding = ViewholderLanguageBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanguageModel language = languages.get(position);
        holder.binding.flagName.setText(language.getName());

        Glide.with(holder.itemView.getContext())
                .load(language.getImg())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.flagImg);

        if (position == selectedPosition) {
            holder.binding.getRoot().setBackgroundResource(R.drawable.choose_button_stroke);
            holder.binding.flagName.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
            holder.binding.flagName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.binding.getRoot().setBackgroundResource(R.drawable.green_button_stroke);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                int previousPosition = selectedPosition;
                selectedPosition = currentPosition;
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                listener.onItemClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public void updateList(List<LanguageModel> newList) {
        languages.clear();
        languages.addAll(newList);
        notifyDataSetChanged();
    }

    public List<LanguageModel> getCurrentList() {
        return new ArrayList<>(languages);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderLanguageBinding binding;

        public ViewHolder(ViewholderLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}