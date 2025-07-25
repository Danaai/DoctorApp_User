package com.example.doctorapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.doctorapp.Domain.CategoryModel;
import com.example.doctorapp.R;
import com.example.doctorapp.databinding.ViewholderCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<CategoryModel> categories;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CategoryModel category);
    }

    public CategoryAdapter(Context context, List<CategoryModel> categories) {
        this.context = context;
        this.categories = categories;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = categories.get(position);
        holder.bind(category, listener);

        // Áp dụng màu nền động
        int[] backgrounds = {
                R.drawable.blue_rec_bg,
                R.drawable.blue_btn_bg,
                R.drawable.purple_rec_bg,
                R.drawable.orange_rec_bg
        };
        int backgroundRes = backgrounds[position % backgrounds.length];
        holder.binding.getRoot().setBackgroundResource(backgroundRes);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.binding.picCat);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateList(List<CategoryModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return categories.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return categories.get(oldPos).getId() == newList.get(newPos).getId();
            }
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return categories.get(oldPos).equals(newList.get(newPos));
            }
        });
        categories.clear();
        categories.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderCategoryBinding binding;

        public ViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CategoryModel category, OnItemClickListener listener) {
            binding.titleCat.setText(category.getName() != null ? category.getName() : "N/A");

            Glide.with(binding.getRoot().getContext())
                    .load(category.getPicture())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(android.R.drawable.ic_menu_gallery))
                    .into(binding.picCat);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(category);
                    }
                }
            });
        }
    }
}