package com.example.drive_kit.View;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drive_kit.R;

import java.util.List;

public class InsuranceListAdapter extends RecyclerView.Adapter<InsuranceListAdapter.VH> {

    public interface OnCompanyClick {
        void onClick(com.example.drive_kit.View.InsuranceCompany company);
    }

    private final List<com.example.drive_kit.View.InsuranceCompany> items;
    private final OnCompanyClick listener;

    public InsuranceListAdapter(List<com.example.drive_kit.View.InsuranceCompany> items, OnCompanyClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insurance_company, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        com.example.drive_kit.View.InsuranceCompany c = items.get(position);
        holder.nameTv.setText(c.getName());
        holder.phoneTv.setText(c.getPhone());

        holder.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView nameTv, phoneTv, partnerTv;

        VH(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.companyName);
            phoneTv = itemView.findViewById(R.id.companyPhone);
        }
    }
}
