package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterPdfUser;
import com.example.bookapp.model.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    private ArrayList<ModelPdf> filterList;
    private AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
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
        adapterPdfUser.setPDFArrayList((ArrayList<ModelPdf>) results.values);

        adapterPdfUser.notifyDataSetChanged();
    }
}
