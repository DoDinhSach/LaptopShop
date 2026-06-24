package com.example.laptopshop.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.PhieuNhap;

import java.util.ArrayList;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.VH> {

    interface Listener {
        void onClick(PhieuNhap receipt);
    }

    private final ArrayList<PhieuNhap> data = new ArrayList<>();
    private final Listener listener;

    ReceiptAdapter(Listener listener) {
        this.listener = listener;
    }

    void setData(ArrayList<PhieuNhap> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receipt, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PhieuNhap receipt = data.get(position);
        if (h.tvReceiptCode != null) h.tvReceiptCode.setText(receipt.getDisplayCode());
        if (h.tvReceiptDate != null) h.tvReceiptDate.setText(ReceiptUiFormatter.formatDate(receipt.createdAt));
        if (h.tvReceiptSupplier != null) h.tvReceiptSupplier.setText(h.itemView.getContext().getString(R.string.receipt_supplier_name_value, valueOrDash(receipt.supplierName)));
        if (h.tvReceiptCreator != null) h.tvReceiptCreator.setText(h.itemView.getContext().getString(R.string.receipt_creator_name_value, valueOrDash(receipt.creatorName)));
        if (h.tvReceiptNote != null) {
            if (receipt.note == null || receipt.note.trim().isEmpty()) {
                h.tvReceiptNote.setText(h.itemView.getContext().getString(R.string.receipt_note_value, h.itemView.getContext().getString(R.string.receipt_note_empty)));
            } else {
                h.tvReceiptNote.setText(h.itemView.getContext().getString(R.string.receipt_note_value, receipt.note.trim()));
            }
        }
        if (h.tvReceiptLineCount != null) h.tvReceiptLineCount.setText(h.itemView.getContext().getString(R.string.receipt_line_count_value, Math.max(0, receipt.lineCount)));
        if (h.tvReceiptQuantity != null) h.tvReceiptQuantity.setText(h.itemView.getContext().getString(R.string.receipt_quantity_value, receipt.totalQuantity));
        if (h.tvReceiptAmount != null) h.tvReceiptAmount.setText(ReceiptUiFormatter.formatCurrency(h.itemView.getContext(), receipt.totalAmount));
        if (h.tvReceiptStatus != null) ReceiptUiFormatter.applyStatusBadge(h.tvReceiptStatus, receipt.status);
        h.itemView.setOnClickListener(v -> listener.onClick(receipt));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvReceiptCode;
        final TextView tvReceiptDate;
        final TextView tvReceiptStatus;
        final TextView tvReceiptSupplier;
        final TextView tvReceiptCreator;
        final TextView tvReceiptNote;
        final TextView tvReceiptLineCount;
        final TextView tvReceiptQuantity;
        final TextView tvReceiptAmount;

        VH(@NonNull View itemView) {
            super(itemView);
            tvReceiptCode = itemView.findViewById(R.id.tvReceiptCode);
            tvReceiptDate = itemView.findViewById(R.id.tvReceiptDate);
            tvReceiptStatus = itemView.findViewById(R.id.tvReceiptStatus);
            tvReceiptSupplier = itemView.findViewById(R.id.tvReceiptSupplier);
            tvReceiptCreator = itemView.findViewById(R.id.tvReceiptCreator);
            tvReceiptNote = itemView.findViewById(R.id.tvReceiptNote);
            tvReceiptLineCount = itemView.findViewById(R.id.tvReceiptLineCount);
            tvReceiptQuantity = itemView.findViewById(R.id.tvReceiptQuantity);
            tvReceiptAmount = itemView.findViewById(R.id.tvReceiptAmount);
        }
    }
}
