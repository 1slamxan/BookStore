package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {
    private String category;
    private ActivityCategoryAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.submitBtn.setOnClickListener(v -> validateData());
        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


    }

    private void validateData() {

        category = binding.categoryEt.getText().toString().trim();

        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Пожалуйста, укажите категорию!", Toast.LENGTH_SHORT).show();
        } else {
            addCategoryFireBase();
        }
    }

    private void addCategoryFireBase() {
        progressDialog.setMessage("Добавление категории");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", timestamp);
        hashMap.put("category", category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(CategoryAddActivity.this, "Категория добавлена успешно", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CategoryAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}