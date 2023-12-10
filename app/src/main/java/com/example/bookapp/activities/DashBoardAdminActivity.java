package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.adapter.AdapterCategory;
import com.example.bookapp.databinding.ActivityDashBoardAdminBinding;
import com.example.bookapp.model.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashBoardAdminActivity extends AppCompatActivity {

    private ActivityDashBoardAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterCategory.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });

        binding.addCategoryBtn.setOnClickListener(v -> startActivity(new Intent(DashBoardAdminActivity.this, CategoryAddActivity.class)));

        binding.addPdfBtn.setOnClickListener(v -> startActivity(new Intent(DashBoardAdminActivity.this, PdfAddActivity.class)));
        binding.profileBtn.setOnClickListener(v -> startActivity(new Intent(DashBoardAdminActivity.this, ProfileActivity.class)));

    }

    private void loadCategories() {
        categoryArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    if (modelCategory != null) {
                        categoryArrayList.add(modelCategory);
                    }
                }

                if (adapterCategory == null) {
                    adapterCategory = new AdapterCategory(DashBoardAdminActivity.this, categoryArrayList);
                    binding.categoriesRV.setAdapter(adapterCategory);
                } else {
                    adapterCategory.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(DashBoardAdminActivity.this, MainActivity.class));
            finish();
        } else
            binding.subTitleTV.setText(firebaseUser.getEmail());

    }
}