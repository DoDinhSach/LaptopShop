package com.example.laptopshop.ui.orders;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.DonHang;
import com.example.laptopshop.data.model.OrderStatus;
import com.example.laptopshop.data.model.PaymentStatus;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(DonHang order);
    }

    private final ArrayList<DonHang> list = new ArrayList<>();
    private final OnOrderClickListener listener;
    private boolean isAdmin = false;

    public OrdersAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setData(ArrayList<DonHang> data) {
        setData(data, false);
    }

    public void setData(ArrayList<DonHang> data, boolean isAdmin) {
        this.isAdmin = isAdmin;
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonHang order = list.get(position);
        holder.bind(order, isAdmin);
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSub, tvMeta, tvStatusChip, tvPaymentChip, tvTotal;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSub = itemView.findViewById(R.id.tvSub);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvStatusChip = itemView.findViewById(R.id.tvStatusChip);
            tvPaymentChip = itemView.findViewById(R.id.tvPaymentChip);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }

        public void bind(DonHang order, boolean isAdmin) {
            tvTitle.setText("Đơn #" + order.id);
            tvSub.setText(df.format(new Date(order.ngayTao)));
            tvTotal.setText(nf.format(order.tongTien) + "đ");
            
            String meta = order.itemCount + " sản phẩm";
            if (isAdmin && !TextUtils.isEmpty(order.username)) {
                meta += " • Khách: " + order.username;
            }
            tvMeta.setText(meta);

            tvStatusChip.setText(formatOrderStatus(itemView.getContext(), order.trangThaiDon));
            applyStatusStyle(tvStatusChip, order.trangThaiDon);

            tvPaymentChip.setText(formatPaymentStatus(itemView.getContext(), order.trangThaiThanhToan));
            applyPaymentStyle(tvPaymentChip, order.trangThaiThanhToan);
        }

        private void applyStatusStyle(TextView view, String status) {
            int color;
            int bg;
            if (OrderStatus.STATUS_CHO_XAC_NHAN.equals(status)) {
                color = R.color.admin_warning;
                bg = R.color.admin_warning_soft;
            } else if (OrderStatus.STATUS_DANG_XU_LY.equals(status)) {
                color = R.color.admin_primary;
                bg = R.color.admin_surface_soft;
            } else if (OrderStatus.STATUS_DA_GIAO.equals(status)) {
                color = R.color.admin_success;
                bg = R.color.admin_success_soft;
            } else {
                color = R.color.admin_danger;
                bg = R.color.admin_danger_soft;
            }
            view.setTextColor(ContextCompat.getColor(itemView.getContext(), color));
            view.setBackgroundResource(R.drawable.bg_badge);
            view.getBackground().setTint(ContextCompat.getColor(itemView.getContext(), bg));
        }

        private void applyPaymentStyle(TextView view, String status) {
            int color;
            int bg;
            if (PaymentStatus.STATUS_DA_THANH_TOAN.equals(status)) {
                color = R.color.admin_success;
                bg = R.color.admin_success_soft;
            } else if (PaymentStatus.STATUS_CHO_THANH_TOAN.equals(status)) {
                color = R.color.admin_warning;
                bg = R.color.admin_warning_soft;
            } else {
                color = R.color.admin_danger;
                bg = R.color.admin_danger_soft;
            }
            view.setTextColor(ContextCompat.getColor(itemView.getContext(), color));
            view.setBackgroundResource(R.drawable.bg_badge);
            view.getBackground().setTint(ContextCompat.getColor(itemView.getContext(), bg));
        }
    }

    public static String formatOrderStatus(Context ctx, String status) {
        if (OrderStatus.STATUS_CHO_XAC_NHAN.equals(status)) return "Chờ xác nhận";
        if (OrderStatus.STATUS_DANG_XU_LY.equals(status)) return "Đang xử lý";
        if (OrderStatus.STATUS_DA_GIAO.equals(status)) return "Đã giao";
        if (OrderStatus.STATUS_DA_HUY.equals(status)) return "Đã hủy";
        return status;
    }

    public static String formatPaymentStatus(Context ctx, String status) {
        if (PaymentStatus.STATUS_CHUA_THANH_TOAN.equals(status)) return "Chưa thanh toán";
        if (PaymentStatus.STATUS_CHO_THANH_TOAN.equals(status)) return "Chờ thanh toán";
        if (PaymentStatus.STATUS_DA_THANH_TOAN.equals(status)) return "Đã thanh toán";
        if (PaymentStatus.STATUS_HET_HAN_THANH_TOAN.equals(status)) return "Hết hạn thanh toán";
        return status;
    }
}
