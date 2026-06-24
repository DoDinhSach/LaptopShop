package com.example.laptopshop.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.utils.InventoryPolicy;
import com.example.laptopshop.utils.ProductImageLoader;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Adapter quản lý việc hiển thị từng dòng sản phẩm trong danh sách của Admin
 */
public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    // Giao diện (Interface) để gửi các sự kiện bấm nút về cho Activity xử lý
    public interface Listener {
        void onEdit(SanPham product);       // Sự kiện bấm nút Sửa
        void onToggleSale(SanPham product); // Sự kiện bấm nút Ngưng/Tiếp tục bán
        void onDelete(SanPham product);     // Sự kiện bấm nút Xóa
    }

    private final ArrayList<SanPham> data = new ArrayList<>(); // Danh sách dữ liệu laptop
    private final Listener listener;                           // Đối tượng nhận sự kiện
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN")); // Định dạng tiền VNĐ

    public AdminProductAdapter(Listener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách dữ liệu mới và làm mới giao diện
     */
    public void setData(ArrayList<SanPham> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged(); // Thông báo cho danh sách vẽ lại các dòng
    }

    /**
     * Tạo ra giao diện cho một dòng sản phẩm (CardView)
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
        return new VH(v);
    }

    /**
     * Đổ dữ liệu từ đối tượng SanPham vào các thành phần UI của một dòng cụ thể
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SanPham p = data.get(position);
        Context context = h.itemView.getContext();

        // Xử lý hiển thị Tên, Hãng và Mô tả (nếu trống thì hiện thông báo mặc định)
        String name = p.tenSanPham == null || p.tenSanPham.trim().isEmpty()
                ? context.getString(R.string.admin_product_unknown_name)
                : p.tenSanPham.trim();
        String brand = p.hang == null || p.hang.trim().isEmpty()
                ? context.getString(R.string.admin_product_unknown_brand)
                : p.hang.trim();
        String desc = p.moTa == null || p.moTa.trim().isEmpty()
                ? context.getString(R.string.admin_product_empty_desc)
                : p.moTa.trim();

        h.tvName.setText(name);
        h.tvBrand.setText(brand);
        h.tvId.setText(context.getString(R.string.admin_product_id, p.maSanPham));
        h.tvDesc.setText(desc);
        h.tvPrice.setText(context.getString(R.string.admin_price_currency, currencyFormat.format(p.gia)));
        h.tvStock.setText(context.getString(R.string.admin_product_stock_short, Math.max(0, p.tonKho)));
        
        // Gọi thư viện nạp ảnh cho laptop
        ProductImageLoader.load(h.ivThumb, p.tenAnh, p.tenSanPham, p.hang);

        // Hiển thị phần trăm giảm giá nếu có
        if (p.giamGia > 0) {
            h.tvDiscount.setVisibility(View.VISIBLE);
            h.tvDiscount.setText(context.getString(R.string.admin_product_discount_short, p.giamGia));
        } else {
            h.tvDiscount.setVisibility(View.GONE);
        }

        // Cập nhật nhãn trạng thái (Còn hàng/Hết hàng/Ngưng bán) và các nút bấm
        bindStatus(context, h, p);
        bindSaleAction(context, h, p);

        // Gán sự kiện cho các nút bấm
        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnToggleSale.setOnClickListener(v -> listener.onToggleSale(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Cập nhật màu sắc và nội dung nhãn trạng thái kho hàng
     */
    private void bindStatus(Context context, VH h, SanPham product) {
        if (!product.isActive) {
            // Trạng thái đã ngưng kinh doanh
            h.tvStatus.setText(R.string.admin_product_status_stopped);
            h.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.admin_text_secondary));
            h.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.admin_product_status_stopped_bg)));
            h.tvStock.setTextColor(ContextCompat.getColor(context, R.color.admin_text_secondary));
            return;
        }

        // Trạng thái dựa trên số lượng tồn kho (Theo chính sách InventoryPolicy)
        InventoryPolicy.StatusAppearance appearance = InventoryPolicy.getAppearance(context, product.tonKho);
        h.tvStatus.setText(appearance.label);
        h.tvStatus.setTextColor(appearance.labelColor);
        h.tvStatus.setBackgroundTintList(appearance.labelBackgroundTint);
        h.tvStock.setTextColor(appearance.stockColor);
    }

    /**
     * Thay đổi icon và nội dung nút Ngưng bán / Tiếp tục bán
     */
    private void bindSaleAction(Context context, VH h, SanPham product) {
        if (product.isActive) {
            h.btnToggleSale.setContentDescription(context.getString(R.string.admin_stop_selling_product_action));
            h.btnToggleSale.setIconResource(R.drawable.ic_stop_selling);
            h.btnToggleSale.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.admin_warning)));
            return;
        }

        h.btnToggleSale.setContentDescription(context.getString(R.string.admin_resume_selling_product_action));
        h.btnToggleSale.setIconResource(R.drawable.ic_resume_selling);
        h.btnToggleSale.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.admin_success)));
    }

    /**
     * Lớp lưu trữ các thành phần UI của một dòng để tối ưu hiệu năng cuộn
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvBrand, tvId, tvDesc, tvPrice, tvStock, tvDiscount, tvStatus;
        MaterialButton btnEdit, btnToggleSale, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvId = itemView.findViewById(R.id.tvId);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggleSale = itemView.findViewById(R.id.btnToggleSale);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
