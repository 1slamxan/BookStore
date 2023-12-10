package com.example.bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.activities.PdfListAdminActivity;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.example.bookapp.filter.FilterCategory;
import com.example.bookapp.model.ModelCategory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {
    private Context context;
    private ArrayList<ModelCategory> categoryArrayList;
    private ArrayList<ModelCategory> filterList;
    private RowCategoryBinding binding;
    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, ArrayList<ModelCategory> filterList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = filterList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position);

        holder.categoryTV.setText(model.getCategory());
        holder.deleteBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Удалить")
                    .setMessage("Вы уверены, что хотите удалить эту категорию?")
                    .setPositiveButton("Подтвердить", (dialog, which) -> {
                        Toast.makeText(context, "Удаление...", Toast.LENGTH_SHORT).show();
                        deleteCategory(model);
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfListAdminActivity.class);
            intent.putExtra("categoryId", model.getId());
            intent.putExtra("categoryTitle", model.getCategory());
            context.startActivity(intent);

        });
    }

    private void deleteCategory(ModelCategory model) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(model.getId())
                .removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(context, "Успешно удалено!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    public void setCategoryArrayList(ArrayList<ModelCategory> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }

    class HolderCategory extends RecyclerView.ViewHolder {
        TextView categoryTV;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            categoryTV = binding.categoryTV;
            deleteBtn = binding.deleteBtn;
        }

    }
}
