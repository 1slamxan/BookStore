package com.example.bookapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.activities.PdfDetailUnBoughtActivity;
import com.example.bookapp.databinding.RowPdfUserBinding;
import com.example.bookapp.filter.FilterPdfUser;
import com.example.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {
    private final Context context;
    FirebaseAuth firebaseAuth;
    boolean isInMyCart = false;
    private ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfUserBinding binding;
    private FilterPdfUser filter;

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return new AdapterPdfUser.HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);

        holder.titleTV.setText(modelPdf.getTitle());
        holder.authorTV.setText(modelPdf.getAuthor());
        holder.priceTV.setText(modelPdf.getPrice() + "$");

        MyApplication.loadCategory(modelPdf.getCategoryId(), holder.categoryTV);
        MyApplication.loadPdfFromUrl(modelPdf.getTitle(), modelPdf.getUrl(), holder.progressBar, holder.pdfView);

        holder.itemView.setOnClickListener(v -> {
            checkCart(modelPdf.getId());
        });
    }

    private void checkCart(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Cart").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyCart = snapshot.exists();
                        if (isInMyCart) {
                            Intent intent = new Intent(context, PdfDetailUnBoughtActivity.class);
                            intent.putExtra("bookId", id);
                            context.startActivity(intent);
                        } else {
                            checkBuys(id);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
    }

    private void checkBuys(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookId = snapshot.child("id").getValue(String.class);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(Objects.requireNonNull(firebaseAuth.getUid())).child("Buys")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() == null) {
                                            Intent intent = new Intent(context, PdfDetailUnBoughtActivity.class);
                                            intent.putExtra("bookId", id);
                                            context.startActivity(intent);
                                        } else {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String boughtBook = ds.child("bookId").getValue(String.class);
                                                Intent intent;
                                                if (bookId != null && bookId.equals(boughtBook)) {
                                                    intent = new Intent(context, PdfDetailActivity.class);
                                                } else {
                                                    intent = new Intent(context, PdfDetailUnBoughtActivity.class);
                                                }
                                                intent.putExtra("bookId", id);
                                                context.startActivity(intent);
                                            }
                                        }
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

    public void changeListItem(ModelPdf modelPdf, HolderPdfUser holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(modelPdf.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookId = snapshot.child("id").getValue(String.class);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Buys")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getValue() != null) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String boughtBook = ds.child("bookId").getValue(String.class);
                                                if (bookId.equals(boughtBook)) {
                                                    holder.priceTV.setVisibility(View.GONE);
                                                    holder.categoryTV.setVisibility(View.GONE);
                                                    holder.priceLabelTV.setText(holder.categoryTV.getText().toString().trim());
                                                    holder.authorTV.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1);
                                                }
                                            }
                                        }
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


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    public void setPDFArrayList(ArrayList<ModelPdf> pdfArrayList) {
        this.pdfArrayList = pdfArrayList;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder {
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, authorTV, categoryTV, priceTV, priceLabelTV;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTV = binding.titleTV;
            authorTV = binding.authorTV;
            categoryTV = binding.categoryTV;
            priceTV = binding.priceTV;
            priceLabelTV = binding.priceLabelTV;
        }
    }
}
