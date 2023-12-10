package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;
    private final ArrayList<String> categoryTitleArrayList = new ArrayList<>();
    private final ArrayList<String> categoryIdArrayList = new ArrayList<>();
    private String title;
    private String description;
    private String author;
    private String price;
    private ActivityPdfAddBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите");
        progressDialog.setCanceledOnTouchOutside(false);
        loadPdfCategories();

        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.attachBtn.setOnClickListener(v -> pdfPickIntent());
        binding.categoryTV.setOnClickListener(v -> categoryPickDialog());
        binding.submitBtn.setOnClickListener(v -> validateData());


    }

    private void validateData() {
        Log.d(TAG, "validateData: validating data");

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
        } else if (pdfUri == null) {
            Toast.makeText(this, "Выберите PDF!", Toast.LENGTH_SHORT).show();
        } else {
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading pdf to storage");

        progressDialog.setMessage("Загрузка PDF...");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        StorageReference reference = FirebaseStorage.getInstance().getReference("Books/" + timestamp);
        reference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: PDF uploaded");
                    Log.d(TAG, "onSuccess: getting pdf url");
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadPdfUrl = "" + uriTask.getResult();

                    uploadPdfInfoToDb(uploadPdfUrl, timestamp);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onFailure: Загрузка PDF не удалась из-за " + e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "агрузка PDF не удалась из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void uploadPdfInfoToDb(String uploadPdfUrl, String timestamp) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading info to firebase db");

        progressDialog.setMessage("Загрузка информации в кмиге");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("author", ""+author);
        hashMap.put("description", ""+description);
        hashMap.put("price", ""+price);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: Successfully uploaded");
                    Toast.makeText(PdfAddActivity.this, "Успешно загружено", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Загрузка не удалась из-за" + e.getMessage());
                    Toast.makeText(PdfAddActivity.this, "Загрузка не удалась из-за" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadCategories: Loading categories");


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryIdArrayList.add(categoryId);
                    categoryTitleArrayList.add(categoryTitle);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];

        for (int i = 0; i < categoriesArray.length; i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryTitle = categoryTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);

                    binding.categoryTV.setText(selectedCategoryTitle);
                    Log.d(TAG, "categoryPickDialog: selected category " + selectedCategoryId + " " +selectedCategoryTitle);
                }).show();

    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting PDF pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF picked");
                pdfUri = data != null ? data.getData() : null;
                Log.d(TAG, "onActivityResult: URI:" + pdfUri);

            }
        } else {
            Log.d(TAG, "onActivityResult: cancelled picked pdf");
            Toast.makeText(this, "Отмененный выбранный PDF", Toast.LENGTH_SHORT).show();
        }
    }
}