package com.example.laptopshop.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.NhaCungCap;

import java.util.ArrayList;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.VH> {

    interface Listener {
        void onClick(NhaCungCap supplier);
        void onLongClick(NhaCungCap supplier);
    }

    interface OnClick {
        void handle(NhaCungCap supplier);
    }

    interface OnLongClick {
        void handle(NhaCungCap supplier);
    }

    private final ArrayList<NhaCungCap> data = new ArrayList<>();
    private final Listener listener;


    SupplierAdapter(OnClick click, OnLongClick longClick) {
        this.listener = new Listener() {
            @Override
            public void onClick(NhaCungCap supplier) {
                click.handle(supplier);
            }

            @Override
            public void onLongClick(NhaCungCap supplier) {
                longClick.handle(supplier);
            }
        };
    }

    void setData(ArrayList<NhaCungCap> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_supplier, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NhaCungCap supplier = data.get(position);
        h.tvName.setText(supplier.name);
        h.tvCode.setText(h.itemView.getContext().getString(R.string.supplier_code_format, supplier.id));
        h.tvStatus.setText(R.string.supplier_status_active);
        bindOptionalText(h.layoutBrand, h.tvBrand, supplier.brand);
        bindOptionalText(h.layoutPhone, h.tvPhone, supplier.phone);
        bindOptionalText(h.layoutAddress, h.tvAddress, supplier.address);
        h.itemView.setOnClickListener(v -> listener.onClick(supplier));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(supplier);
            return true;
        });
    }

    private void bindOptionalText(View container, TextView textView, String value) {
        String safeValue = value == null ? "" : value.trim();
        if (container != null) container.setVisibility(safeValue.isEmpty() ? View.GONE : View.VISIBLE);
        if (textView != null) textView.setText(safeValue);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvStatus, tvBrand, tvPhone, tvAddress;
        LinearLayout layoutBrand, layoutPhone, layoutAddress;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            layoutBrand = itemView.findViewById(R.id.layoutBrand);
            layoutPhone = itemView.findViewById(R.id.layoutPhone);
            layoutAddress = itemView.findViewById(R.id.layoutAddress);
        }
    }
}
