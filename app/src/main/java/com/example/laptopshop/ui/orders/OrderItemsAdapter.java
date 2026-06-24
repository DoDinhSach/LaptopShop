package com.example.laptopshop.ui.orders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.DonHangItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder> {

    private final ArrayList<DonHangItem> list = new ArrayList<>();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public void setData(ArrayList<DonHangItem> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonHangItem item = list.get(position);
        holder.tvName.setText(item.tenSanPham);
        
        StringBuilder sub = new StringBuilder();
        sub.append(item.soLuong).append(" x ").append(nf.format(item.donGia)).append("đ");
        
        String variants = "";
        if (item.dungLuong != null && !item.dungLuong.trim().isEmpty()) {
            variants += item.dungLuong.trim();
        }
        if (item.mauSac != null && !item.mauSac.trim().isEmpty()) {
            if (!variants.isEmpty()) variants += ", ";
            variants += item.mauSac.trim();
        }
        
        if (!variants.isEmpty()) {
            sub.append(" • ").append(variants);
        }
        
        holder.tvSub.setText(sub.toString());
        holder.tvAmount.setText(nf.format(item.thanhTien) + "đ");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSub, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSub = itemView.findViewById(R.id.tvSub);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
