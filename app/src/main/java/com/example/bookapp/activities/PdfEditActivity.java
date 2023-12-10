package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    private ActivityPdfEditBinding binding;
    private String bookId, selectedCategoryId, selectedCategoryTitle, title, description, author, price;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите!");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.categoryTV.setOnClickListener(v -> categoryDialog());
        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.submitBtn.setOnClickListener(v -> validateData());

    }

    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        author = binding.authorEt.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Введите название!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(author)) {
            Toast.makeText(this, "Введите автора!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Введите описание!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Введите цену!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Выберите категорию!", Toast.LENGTH_SHORT).show();
        } else {
            updatePdf();
        }
    }

    private void updatePdf() {

        progressDialog.setMessage("Обновление информации о книге...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("author", ""+author);
        hashMap.put("description", "" + description);
        hashMap.put("price", price);
        hashMap.put("categoryId", "" + selectedCategoryId);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.keepSynced(true);
        reference.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "Информация о книге обновлена", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfEditActivity.this, "Не удалось обновить из-за" + e.getMessage(), Toast.LENGTH_SHORT).show();

                });


    }

    private void loadBookInfo() {
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books");
        bookRef.keepSynced(true);
        bookRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = snapshot.child("categoryId").getValue(String.class);
                        binding.titleEt.setText(snapshot.child("title").getValue(String.class));
                        binding.authorEt.setText(snapshot.child("author").getValue(String.class));
                        binding.descriptionEt.setText(snapshot.child("description").getValue(String.class));
                        binding.priceEt.setText(snapshot.child("price").getValue(String.class));

                        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Categories");
                        categoryRef.keepSynced(true);
                        categoryRef.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = snapshot.child("category").getValue(String.class);
                                        binding.categoryTV.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void categoryDialog() {
        String[] categoryArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoryArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите категорию")
                .setItems(categoryArray, (dialog, which) -> {
                    selectedCategoryId = categoryIdArrayList.get(which);
                    selectedCategoryTitle = categoryTitleArrayList.get(which);

                    binding.categoryTV.setText(selectedCategoryTitle);
                })
                .show();
    }

    private void loadCategories() {
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    categoryIdArrayList.add(ds.child("id").getValue(String.class));
                    categoryTitleArrayList.add(ds.child("category").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}