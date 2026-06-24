package com.example.laptopshop.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.NhaCungCapDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.NhaCungCap;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class AdminSuppliersActivity extends BaseHomeActivity {

    private NhaCungCapDao nhaCungCapDao;
    private SupplierAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        nhaCungCapDao = new NhaCungCapDao(this);
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_admin_suppliers;
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
        return getString(R.string.admin_suppliers_title);
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
        RecyclerView rvSuppliers = findViewById(R.id.rvSuppliers);
        if (rvSuppliers != null) {
            rvSuppliers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SupplierAdapter(this::showSupplierDialog, this::confirmDeleteSupplier);
            rvSuppliers.setAdapter(adapter);
        }

        View btnAdd = findViewById(R.id.btnAddSupplier);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showSupplierDialog(null));
        }

        EditText edtSearch = findViewById(R.id.edtSupplierSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    loadData(s.toString().trim());
                }
            });
        }

        loadData("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nhaCungCapDao != null) {
            loadData(currentKeyword());
        }
    }

    private void loadData(String keyword) {
        if (nhaCungCapDao == null || adapter == null) return;
        ArrayList<NhaCungCap> suppliers = nhaCungCapDao.getAll(keyword);
        adapter.setData(suppliers);
    }

    private String currentKeyword() {
        EditText edtSearch = findViewById(R.id.edtSupplierSearch);
        return edtSearch == null ? "" : edtSearch.getText().toString().trim();
    }

    private void confirmDeleteSupplier(NhaCungCap supplier) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_supplier_title)
                .setMessage(getString(R.string.supplier_delete_confirm, supplier.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (nhaCungCapDao.hasReceipts(supplier.id)) {
                        Toast.makeText(this, R.string.supplier_in_use, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = nhaCungCapDao.delete(supplier.id);
                    if (!ok) {
                        Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, R.string.supplier_deleted, Toast.LENGTH_SHORT).show();
                    loadData(currentKeyword());
                })
                .show();
    }

    private void showSupplierDialog(NhaCungCap oldSupplier) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_supplier_form, null, false);
        TextView tvTitle = view.findViewById(R.id.tvSupplierDialogTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvSupplierDialogSubtitle);
        EditText edtName = view.findViewById(R.id.edtSupplierName);
        EditText edtBrand = view.findViewById(R.id.edtSupplierBrand);
        EditText edtPhone = view.findViewById(R.id.edtSupplierPhone);
        EditText edtAddress = view.findViewById(R.id.edtSupplierAddress);
        View btnCancel = view.findViewById(R.id.btnCancelSupplier);
        View btnSave = view.findViewById(R.id.btnSaveSupplier);

        boolean editing = oldSupplier != null;
        if (tvTitle != null) tvTitle.setText(editing ? R.string.supplier_form_edit_title : R.string.supplier_form_add_title);
        if (tvSubtitle != null) tvSubtitle.setText(editing ? R.string.supplier_form_edit_subtitle : R.string.supplier_form_add_subtitle);

        if (editing) {
            if (edtName != null) edtName.setText(oldSupplier.name);
            if (edtBrand != null) edtBrand.setText(oldSupplier.brand);
            if (edtPhone != null) edtPhone.setText(oldSupplier.phone);
            if (edtAddress != null) edtAddress.setText(oldSupplier.address);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) {
                return;
            }
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnSave != null) btnSave.setOnClickListener(v -> {
            String name = edtName != null ? edtName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.err_supplier_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            NhaCungCap supplier = editing ? oldSupplier : new NhaCungCap();
            supplier.name = name;
            supplier.brand = edtBrand != null ? edtBrand.getText().toString().trim() : "";
            supplier.phone = edtPhone != null ? edtPhone.getText().toString().trim() : "";
            supplier.address = edtAddress != null ? edtAddress.getText().toString().trim() : "";

            boolean ok = editing ? nhaCungCapDao.update(supplier) : nhaCungCapDao.insert(supplier) != -1;
            if (!ok) {
                Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, editing ? R.string.supplier_updated : R.string.supplier_added, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadData(currentKeyword());
        });
        dialog.show();
    }
}
