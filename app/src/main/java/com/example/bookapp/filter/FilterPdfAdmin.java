package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterPdfAdmin;
import com.example.bookapp.model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    private ArrayList<ModelPdf> filterList;
    private AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    filteredModels.add(filterList.get(i));
                }
            }

            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;

        } else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterPdfAdmin.setPDFArrayList((ArrayList<ModelPdf>) results.values);

        adapterPdfAdmin.notifyDataSetChanged();
    }
}
