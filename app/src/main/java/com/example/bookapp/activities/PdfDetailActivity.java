package com.example.bookapp.activities;

import static com.example.bookapp.utils.Constants.DOWNLOAD_TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapter.AdapterComment;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.AddCommentDialogBinding;
import com.example.bookapp.model.ModelComment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {
    private ActivityPdfDetailBinding binding;
    private String bookId, bookTitle, bookUrl;
    private boolean isInMyFavorite = false;
    private FirebaseAuth firebaseAuth;
    private String comment;
    private ProgressDialog progressDialog;
    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Пожалуйста, подождите!");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkFavorite();
        }
        loadDetails();
        loadComments();
        MyApplication.incrementBookViewCount(bookId);

        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.readBookBtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
            intent1.putExtra("bookId", bookId);
            startActivity(intent1);
        });

        binding.favoriteBtn.setOnClickListener(v -> {
            if (isInMyFavorite) {
                MyApplication.removeFavorite(PdfDetailActivity.this, bookId);
            } else {
                MyApplication.addFavorite(PdfDetailActivity.this, bookId);
            }
        });

        binding.addCommentBtn.setOnClickListener(v -> {
            if (firebaseAuth.getUid() == null) {
                Toast.makeText(PdfDetailActivity.this, "Вы не вошли в систему...", Toast.LENGTH_SHORT).show();
            } else {
                addCommentDialog();
            }
        });
    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelComment modelComment = ds.getValue(ModelComment.class);

                            commentArrayList.add(modelComment);
                        }
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);
                        binding.commentRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addCommentDialog() {
        AddCommentDialogBinding commentDialogBinding = AddCommentDialogBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentDialogBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentDialogBinding.backBtn.setOnClickListener(v -> alertDialog.dismiss());
        commentDialogBinding.submitBtn.setOnClickListener(v -> {
            comment = commentDialogBinding.commentEt.getText().toString().trim();
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(PdfDetailActivity.this, "Введите свой комментарий...", Toast.LENGTH_SHORT).show();
            } else {
                alertDialog.dismiss();
                addComment();
            }
        });
    }

    private void addComment() {
        progressDialog.setMessage("Добавление комментария");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("bookId", "" + bookId);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.keepSynced(true);
        reference.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(PdfDetailActivity.this, "Комментарий Добавлен...", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PdfDetailActivity.this, "Не удалось добавить комментарий из-за: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = snapshot.child("title").getValue(String.class);
                        bookUrl = snapshot.child("url").getValue(String.class);


                        MyApplication.loadCategory(snapshot.child("categoryId").getValue(String.class), binding.categoryTV);
                        MyApplication.loadPdfFromUrl(bookTitle, bookUrl, binding.progressBar, binding.pdfView);

                        binding.titleTV.setText(bookTitle);
                        binding.authorTV.setText(snapshot.child("author").getValue(String.class));
                        binding.descriptionTV.setText(snapshot.child("description").getValue(String.class));
                        binding.viewsTV.setText(""+snapshot.child("viewsCount").getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkFavorite() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite) {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Удалить из избранного");
                        } else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Добавить в избранное");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}