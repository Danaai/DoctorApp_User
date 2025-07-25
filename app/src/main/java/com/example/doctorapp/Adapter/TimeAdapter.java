package com.example.doctorapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.R;
import com.example.doctorapp.databinding.ViewholderDateBinding;
import com.example.doctorapp.databinding.ViewholderTimeBinding;

import java.util.ArrayList;
import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewholder> {
    private final List<String> timeSlots;
    private List<String> bookedTimes = new ArrayList<>();
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private OnItemClickListener listener;

    public TimeAdapter(List<String> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public void setBookedTimes(List<String> bookedTimes) {
        this.bookedTimes = bookedTimes != null ? bookedTimes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public String getSelectedTime() {
        return selectedPosition != -1 ? timeSlots.get(selectedPosition) : null;
    }

    @NonNull
    @Override
    public TimeViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderTimeBinding binding = ViewholderTimeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TimeViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeAdapter.TimeViewholder holder, int position) {
        holder.bind(timeSlots.get(position), position, this);
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public class TimeViewholder extends RecyclerView.ViewHolder {
        private final ViewholderTimeBinding binding;

        public TimeViewholder(ViewholderTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String time, int position, TimeAdapter adapter) {
            binding.timeTxt.setText(time);
            boolean isBooked = bookedTimes.contains(time);

            if (isBooked) {
                binding.timeTxt.setBackgroundResource(R.drawable.notes_bg);
                binding.timeTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.lightGrey));
                binding.getRoot().setEnabled(false);
            } else {
                binding.getRoot().setEnabled(true);
                if(adapter.selectedPosition == position) {
                    binding.timeTxt.setBackgroundResource(R.drawable.blue_btn_bg);
                    binding.timeTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
                } else {
                    binding.timeTxt.setBackgroundResource(R.drawable.light_gray_bg);
                    binding.timeTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.black));
                }

                binding.getRoot().setOnClickListener(v -> {
                    adapter.lastSelectedPosition = adapter.selectedPosition;
                    adapter.selectedPosition = position;
                    adapter.notifyItemChanged(adapter.lastSelectedPosition);
                    adapter.notifyItemChanged(adapter.selectedPosition);
                    if(adapter.listener != null) {
                        adapter.listener.onItemClick(time);
                    }
                });
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String time);
    }
}
