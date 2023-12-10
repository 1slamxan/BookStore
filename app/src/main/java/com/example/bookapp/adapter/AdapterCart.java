package com.example.bookapp.adapter;

import static com.example.bookapp.utils.Constants.FAVORITE_BOOK_TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailUnBoughtActivity;
import com.example.bookapp.databinding.RowPdfCartBinding;
import com.example.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.HolderPdfCart> {
    private final Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfCartBinding binding;
    private FirebaseAuth firebaseAuth;

    public AdapterCart(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfCart onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfCartBinding.inflate(LayoutInflater.from(context), parent, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return new HolderPdfCart(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfCart holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);
        loadBookDetails(modelPdf, holder);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfDetailUnBoughtActivity.class);
            intent.putExtra("bookId", modelPdf.getId());
            context.startActivity(intent);
        });

        holder.cartBtn.setOnClickListener(v -> {
            MyApplication.removeCart(context, modelPdf.getId());
        });

    }

    private void loadBookDetails(ModelPdf model, HolderPdfCart holder) {
        Log.d(FAVORITE_BOOK_TAG, "loadBookDetails: Book details of book with id: " + model.getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(model.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelPdf modelPdf = snapshot.getValue(ModelPdf.class);

                        model.setFavorite(true);
                        model.setCart(true);
                        model.setTitle(modelPdf.getTitle());
                        model.setAuthor(modelPdf.getAuthor());
                        model.setDescription(modelPdf.getDescription());
                        model.setPrice(modelPdf.getPrice());
                        model.setTimestamp(modelPdf.getTimestamp());
                        model.setCategoryId(modelPdf.getCategoryId());
                        model.setUid(modelPdf.getUid());
                        model.setUrl(modelPdf.getUrl());

                        MyApplication.loadPdfFromUrl(modelPdf.getTitle(), modelPdf.getUrl(), holder.progressBar, holder.pdfView);

                        holder.titleTV.setText(modelPdf.getTitle());
                        holder.authorTv.setText(modelPdf.getAuthor());
                        holder.priceTV.setText(modelPdf.getPrice() + "$");

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

    class HolderPdfCart extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, authorTv, priceTV;

        ImageButton cartBtn;

        public HolderPdfCart(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTV = binding.titleTV;
            authorTv = binding.authorTV;
            priceTV = binding.priceTV;
            cartBtn = binding.cartBtn;
        }
    }
}
