package com.example.bookapp.activities;

import static com.example.bookapp.utils.Constants.MAX_BYTES_PDF;
import static com.example.bookapp.utils.Constants.PDF_LOAD_SINGLE_TAG;
import static com.example.bookapp.utils.Constants.PDF_VIEW_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfViewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {
    private ActivityPdfViewBinding binding;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(PDF_LOAD_SINGLE_TAG, "onCreate: BookId " + bookId);

        loadBookDetails();
        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }

    private void loadBookDetails() {
        Log.d(PDF_VIEW_TAG, "loadBookDetails: Get PDF url...");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.keepSynced(true);
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(PDF_VIEW_TAG, "onDataChange: PDF url");
                        loadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBookFromUrl(String pdfUrl) {
        Log.d(PDF_VIEW_TAG, "loadBookFromUrl: Get url from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    binding.pdfView.fromBytes(bytes)
                            .swipeHorizontal(false)
                            .onPageChange((page, pageCount) -> {
                                binding.toolbarSubTitleTV.setText((page + 1) + "/" + pageCount);
                                Log.d(PDF_VIEW_TAG, "loadBookFromUrl: " + (page + 1) + "/" + pageCount);
                            })
                            .onError(t -> {
                                Log.d(PDF_VIEW_TAG, "loadBookFromUrl: " + t.getMessage());
                                Toast.makeText(PdfViewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .onPageError((page, t) -> {
                                Log.d(PDF_VIEW_TAG, "loadBookFromUrl: " + t.getMessage());
                                Toast.makeText(PdfViewActivity.this, "Error page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .load();
                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.d(PDF_VIEW_TAG, "loadBookFromUrl: " + e.getMessage());
                    binding.progressBar.setVisibility(View.GONE);
                });
    }
}