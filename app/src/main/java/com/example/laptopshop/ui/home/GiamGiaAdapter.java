package com.example.laptopshop.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.GiamGia;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GiamGiaAdapter extends RecyclerView.Adapter<GiamGiaAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(GiamGia voucher);
        void onDelete(GiamGia voucher);
    }

    private final ArrayList<GiamGia> list = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public GiamGiaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(ArrayList<GiamGia> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiamGia v = list.get(position);
        holder.tvCode.setText(v.code);
        holder.tvDiscount.setText(String.format(Locale.getDefault(), "Giảm %,dđ", v.discountAmount));
        holder.tvInfo.setText(String.format(Locale.getDefault(), "Đơn tối thiểu %,dđ", v.minOrderValue));
        
        if (v.productId != null && v.productName != null) {
            holder.tvTarget.setText("Áp dụng cho: " + v.productName);
        } else {
            holder.tvTarget.setText("Áp dụng cho: Tất cả sản phẩm");
        }
        
        holder.tvExpiry.setText("Hết hạn: " + df.format(new Date(v.expiryDate)));
        
        holder.btnEdit.setOnClickListener(view -> listener.onEdit(v));
        holder.btnDelete.setOnClickListener(view -> listener.onDelete(v));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDiscount, tvInfo, tvTarget, tvExpiry;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvInfo = itemView.findViewById(R.id.tvVoucherInfo);
            tvTarget = itemView.findViewById(R.id.tvVoucherTarget);
            tvExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            btnEdit = itemView.findViewById(R.id.btnEditVoucher);
            btnDelete = itemView.findViewById(R.id.btnDeleteVoucher);
        }
    }
}
