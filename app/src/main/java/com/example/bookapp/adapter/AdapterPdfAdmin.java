package com.example.bookapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.activities.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filter.FilterPdfAdmin;
import com.example.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private final Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;

    public AdapterPdfAdmin(Context context, ArrayList  <ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Пожалуйста, подождите!");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf modelPdf = pdfArrayList.get(position);

        holder.titleTV.setText(modelPdf.getTitle());
        holder.authorTV.setText(modelPdf.getAuthor());
        holder.priceTV.setText(modelPdf.getPrice());

        MyApplication.loadCategory(modelPdf.getCategoryId(), holder.categoryTV);
        MyApplication.loadPdfFromUrl(modelPdf.getTitle(), modelPdf.getUrl(), holder.progressBar, holder.pdfView);

        holder.moreBtn.setOnClickListener(v -> moreOptionsDialog(modelPdf));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PdfDetailActivity.class);
            intent.putExtra("bookId", modelPdf.getId());
            context.startActivity(intent);
        });
    }

    private void moreOptionsDialog(ModelPdf modelPdf) {
        String[] options = {"Редактировать", "Удалить"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите вариант")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(context, PdfEditActivity.class);
                        intent.putExtra("bookId", modelPdf.getId());
                        context.startActivity(intent);
                    } else if (which == 1) {
                        MyApplication.deleteBook(context, modelPdf.getTitle(), modelPdf.getUrl(), modelPdf.getId());
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    public void setPDFArrayList(ArrayList<ModelPdf> pdfArrayList) {
        this.pdfArrayList = pdfArrayList;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTV, authorTV, categoryTV, priceTV;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTV = binding.titleTV;
            authorTV = binding.authorTV;
            categoryTV = binding.categoryTV;
            priceTV = binding.priceTV;
            moreBtn = binding.moreBtn;
        }
    }
}
