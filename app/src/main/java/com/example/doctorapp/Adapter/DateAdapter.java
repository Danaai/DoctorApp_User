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

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewholder> {
    private final List<String> dates;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private OnItemClickListener listener;

    public DateAdapter(List<String> dates) {
        this.dates = dates;
    }

    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; };

    public String getSelectedDate() { return selectedPosition != -1 ? dates.get(selectedPosition) : null; }

    public void updateDates(List<String> newDates) {
        this.dates.clear();
        this.dates.addAll(newDates);
        notifyDataSetChanged();
    };

    @NonNull
    @Override
    public DateViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderDateBinding binding = ViewholderDateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DateViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.DateViewholder holder, int position) {
        holder.bind(dates.get(position), position, this);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public class DateViewholder extends RecyclerView.ViewHolder {
        private final ViewholderDateBinding binding;
        public DateViewholder(ViewholderDateBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
        public void bind(String date, int position, DateAdapter adapter) {
            String [] dateParts = date.split("-");
            if(dateParts.length == 3) {
                binding.dayTxt.setText(dateParts[0]);
                binding.dateMonthTxt.setText(dateParts[1] + " " + dateParts[2]);

                if(adapter.selectedPosition==position) {
                    binding.mainLayout.setBackgroundResource(R.drawable.blue_btn_bg);
                    binding.dayTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
                    binding.dateMonthTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
                } else {
                    binding.mainLayout.setBackgroundResource(R.drawable.light_gray_bg);
                    binding.dayTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.black));
                    binding.dateMonthTxt.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.black));
                }

                binding.getRoot().setOnClickListener( v -> {
                    adapter.lastSelectedPosition=adapter.selectedPosition;
                    adapter.selectedPosition=position;
                    adapter.notifyItemChanged(adapter.lastSelectedPosition);
                    adapter.notifyItemChanged(adapter.selectedPosition);
                    if(adapter.listener != null) {
                        adapter.listener.onItemClick(date);
                    }
                });
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String date);
    }
}
