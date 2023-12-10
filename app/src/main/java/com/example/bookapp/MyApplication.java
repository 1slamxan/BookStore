package com.example.bookapp;

import static com.example.bookapp.utils.Constants.DELETE_BOOK_TAG;
import static com.example.bookapp.utils.Constants.DOWNLOAD_TAG;
import static com.example.bookapp.utils.Constants.MAX_BYTES_PDF;
import static com.example.bookapp.utils.Constants.PDF_LOAD_SINGLE_TAG;
import static com.example.bookapp.utils.Constants.PDF_SIZE_TAG;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    public static final String formatTimestamp(String timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        return DateFormat.format("dd/MM/yyyy", calendar).toString();
    }

    public static void deleteBook(Context context, String bookTitle, String bookUrl, String bookId) {

        Log.d(DELETE_BOOK_TAG, "deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Удаление " + bookTitle + " ...");
        progressDialog.show();

        Log.d(DELETE_BOOK_TAG, "deleteBook: Deleting from storage...");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(DELETE_BOOK_TAG, "onSuccess: Deleted from Storage");

                    Log.d(DELETE_BOOK_TAG, "onSuccess: Deleting info from db...");
                    DatabaseReference referenceDb = FirebaseDatabase.getInstance().getReference("Books");
                    referenceDb.keepSynced(true);
                    referenceDb.child(bookId)
                            .removeValue()
                            .addOnSuccessListener(unused1 -> {
                                Log.d(DELETE_BOOK_TAG, "onSuccess: Info deleted from db");
                                progressDialog.dismiss();
                                Toast.makeText(context, "Книга успешно удалена", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.d(DELETE_BOOK_TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                progressDialog.show();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(DELETE_BOOK_TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                    progressDialog.show();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public static void loadPdfFromUrl(String bookTitle, String bookUrl, ProgressBar progressBar, PDFView pdfView) {
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    Log.d(PDF_LOAD_SINGLE_TAG, "onSuccess: " + bookTitle + " successfully got the file");
                    pdfView.fromBytes(bytes)
                            .pages(0)
                            .spacing(0)
                            .swipeHorizontal(false)
                            .enableSwipe(false)
                            .onError(t -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(PDF_LOAD_SINGLE_TAG, "onError: " + t.getMessage());
                            })
                            .onPageError((page, t) -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(PDF_LOAD_SINGLE_TAG, "onPageError: " + t.getMessage());
                            })
                            .onLoad(nbPages -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d(PDF_LOAD_SINGLE_TAG, "loadPdfFromUrl: pdf loaded");
                            })
                            .load();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.d(PDF_LOAD_SINGLE_TAG, "onFailure: failed getting file from url due to " + e.getMessage());
                });
    }

    public static void loadCategory(String categoryId, TextView categoryTV) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = "" + snapshot.child("category").getValue();
                        categoryTV.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(String bookId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();

                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        long newViewCount = Long.parseLong(viewsCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewCount);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.keepSynced(true);
                        ref.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void addFavorite(Context context, String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", bookId);
            hashMap.put("timestamp", "" + timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Добавлено в список избранных...", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Не удалось добавить в избранное из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static void removeFavorite(Context context, String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Удалены из списка избранных...", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Не удалось удалить из избранного из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static void addCart(Context context, String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", bookId);
            hashMap.put("timestamp", "" + timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Cart").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Добавлено в корзину...", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Не удалось добавить в корзину из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static void removeCart(Context context, String bookId) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Cart").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Удалено из вашей корзины...", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Не удалось удалить из корзины из-за " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
