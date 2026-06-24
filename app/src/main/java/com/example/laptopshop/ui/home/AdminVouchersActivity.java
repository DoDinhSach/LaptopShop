package com.example.laptopshop.ui.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.dao.GiamGiaDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.data.model.GiamGia;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminVouchersActivity extends BaseHomeActivity {

    private GiamGiaDao giamGiaDao;
    private SanPhamDao sanPhamDao;
    private GiamGiaAdapter adapter;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private long selectedExpiryTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        giamGiaDao = new GiamGiaDao(this);
        sanPhamDao = new SanPhamDao(this);
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.content_admin_vouchers;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_admin;
    }

    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_admin_inventory;
    }

    @Override
    protected String screenTitle() {
        return "Quản lý mã giảm giá";
    }

    @Override
    protected boolean shouldShowToolbarActions() {
        return false;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return true;
    }

    @Override
    protected boolean shouldUseAdminBackButtonStyling() {
        return true;
    }

    @Override
    protected void onShellReady() {
        RecyclerView rv = findViewById(R.id.rvVouchers);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new GiamGiaAdapter(new GiamGiaAdapter.Listener() {
                @Override
                public void onEdit(GiamGia voucher) {
                    showVoucherDialog(voucher);
                }

                @Override
                public void onDelete(GiamGia voucher) {
                    confirmDeleteVoucher(voucher);
                }
            });
            rv.setAdapter(adapter);
        }

        View btnAdd = findViewById(R.id.btnAddVoucher);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showVoucherDialog(null));
        }

        EditText edtSearch = findViewById(R.id.edtVoucherSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { loadData(s.toString().trim()); }
            });
        }

        loadData("");
    }

    private void loadData(String keyword) {
        if (giamGiaDao == null) return;
        ArrayList<GiamGia> vouchers = giamGiaDao.getAll();
        if (!keyword.isEmpty()) {
            ArrayList<GiamGia> filtered = new ArrayList<>();
            for (GiamGia v : vouchers) {
                if (v.code.toLowerCase().contains(keyword.toLowerCase())) {
                    filtered.add(v);
                }
            }
            vouchers = filtered;
        }
        if (adapter != null) {
            adapter.setData(vouchers);
        }
    }

    private void confirmDeleteVoucher(GiamGia voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa mã giảm giá")
                .setMessage("Bạn có chắc chắn muốn xóa mã " + voucher.code + "?")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (giamGiaDao.delete(voucher.id)) {
                        Toast.makeText(this, "Đã xóa mã giảm giá", Toast.LENGTH_SHORT).show();
                        loadData("");
                    }
                })
                .show();
    }

    private void showVoucherDialog(GiamGia oldVoucher) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_voucher_form, null, false);
        TextView tvTitle = view.findViewById(R.id.tvVoucherDialogTitle);
        EditText edtCode = view.findViewById(R.id.edtVoucherCode);
        EditText edtAmount = view.findViewById(R.id.edtVoucherAmount);
        EditText edtMinOrder = view.findViewById(R.id.edtVoucherMinOrder);
        Spinner spProduct = view.findViewById(R.id.spVoucherProduct);
        EditText edtExpiry = view.findViewById(R.id.edtVoucherExpiry);
        View btnCancel = view.findViewById(R.id.btnCancelVoucher);
        View btnSave = view.findViewById(R.id.btnSaveVoucher);

        ArrayList<SanPham> products = sanPhamDao.layTatCaChoAdmin();
        ArrayList<String> productNames = new ArrayList<>();
        productNames.add("Tất cả sản phẩm");
        for (SanPham p : products) {
            productNames.add(p.tenSanPham);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduct.setAdapter(spinnerAdapter);

        boolean editing = oldVoucher != null;
        tvTitle.setText(editing ? "Sửa mã giảm giá" : "Thêm mã giảm giá");

        if (editing) {
            edtCode.setText(oldVoucher.code);
            edtAmount.setText(String.valueOf(oldVoucher.discountAmount));
            edtMinOrder.setText(String.valueOf(oldVoucher.minOrderValue));
            selectedExpiryTimestamp = oldVoucher.expiryDate;
            edtExpiry.setText(df.format(new Date(selectedExpiryTimestamp)));
            
            if (oldVoucher.productId != null) {
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).maSanPham == oldVoucher.productId) {
                        spProduct.setSelection(i + 1);
                        break;
                    }
                }
            }
        } else {
            selectedExpiryTimestamp = System.currentTimeMillis() + 7L * 24 * 3600 * 1000;
            edtExpiry.setText(df.format(new Date(selectedExpiryTimestamp)));
        }

        edtExpiry.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedExpiryTimestamp);
            new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                selectedExpiryTimestamp = selected.getTimeInMillis();
                edtExpiry.setText(df.format(selected.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim().toUpperCase();
            String amountStr = edtAmount.getText().toString().trim();
            if (code.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            GiamGia voucher = editing ? oldVoucher : new GiamGia();
            voucher.code = code;
            voucher.discountAmount = Integer.parseInt(amountStr);
            voucher.minOrderValue = Integer.parseInt(edtMinOrder.getText().toString().trim().isEmpty() ? "0" : edtMinOrder.getText().toString().trim());
            voucher.expiryDate = selectedExpiryTimestamp;
            
            int selection = spProduct.getSelectedItemPosition();
            if (selection > 0) {
                voucher.productId = products.get(selection - 1).maSanPham;
            } else {
                voucher.productId = null;
            }

            boolean ok = editing ? giamGiaDao.update(voucher) : giamGiaDao.insert(voucher) != -1;
            if (ok) {
                Toast.makeText(this, "Đã lưu mã giảm giá", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData("");
            } else {
                Toast.makeText(this, "Mã code đã tồn tại hoặc có lỗi", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}
