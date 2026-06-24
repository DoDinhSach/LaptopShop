package com.example.laptopshop.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.dao.PhieuNhapDao;
import com.example.laptopshop.data.dao.NhaCungCapDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.data.model.PhieuNhap;
import com.example.laptopshop.data.model.PhieuNhapItem;
import com.example.laptopshop.data.model.NhaCungCap;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.utils.SessionManager;

import java.util.ArrayList;

public class AdminReceiptEditorActivity extends AppCompatActivity {

    private PhieuNhapDao phieuNhapDao;
    private NhaCungCapDao nhaCungCapDao;
    private SanPhamDao sanPhamDao;
    private ReceiptLineAdapter adapter;
    private ArrayList<NhaCungCap> suppliers;
    private ArrayList<SanPham> availableProducts = new ArrayList<>();
    private String creatorName;
    private TextView tvSupplierBrandHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_editor);

        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new android.content.Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        phieuNhapDao = new PhieuNhapDao(this);
        nhaCungCapDao = new NhaCungCapDao(this);
        sanPhamDao = new SanPhamDao(this);
        suppliers = nhaCungCapDao.getAll(null);
        creatorName = resolveCreatorName();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setContentInsetsRelative(0, 0);
            toolbar.setContentInsetsAbsolute(0, 0);
            toolbar.setTitleMarginStart(Math.round(getResources().getDisplayMetrics().density * 12));
            toolbar.setTitleMarginEnd(0);
            toolbar.setTitleMarginTop(0);
            toolbar.setTitleMarginBottom(0);
            toolbar.setTitle(getString(R.string.receipt_create_title));
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvSupplierBrandHint = findViewById(R.id.tvSupplierBrandHint);
        bindHeader();
        setupSupplierSpinner();
        setupLinesRecycler();

        View btnAdd = findViewById(R.id.btnAddReceiptLine);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> showAddLineDialog());
        View btnSaveDraft = findViewById(R.id.btnSaveDraftReceipt);
        if (btnSaveDraft != null) btnSaveDraft.setOnClickListener(v -> saveReceipt(false));
        View btnConfirm = findViewById(R.id.btnConfirmReceipt);
        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> saveReceipt(true));
        updateSummary();
    }

    private void bindHeader() {
        TextView tvCode = findViewById(R.id.tvReceiptCode);
        TextView tvCreator = findViewById(R.id.tvReceiptCreator);
        if (tvCode != null) tvCode.setText(getString(R.string.receipt_create_title));
        if (tvCreator != null) tvCreator.setText(creatorName);
    }

    private void setupSupplierSpinner() {
        Spinner spSupplier = findViewById(R.id.spSupplier);
        if (spSupplier == null) return;
        ArrayAdapter<String> supplierAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected_admin, new ArrayList<>());
        supplierAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_admin);
        for (NhaCungCap supplier : suppliers) {
            supplierAdapter.add(supplier.name);
        }
        spSupplier.setAdapter(supplierAdapter);
        spSupplier.setOnItemSelectedListener(new SimpleItemSelectedListener(this::refreshAvailableProducts));
        refreshAvailableProducts();
    }

    private void setupLinesRecycler() {
        RecyclerView rv = findViewById(R.id.rvReceiptLines);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceiptLineAdapter(true, new ReceiptLineAdapter.Listener() {
            @Override
            public void onRemove(int position) {
                new AlertDialog.Builder(AdminReceiptEditorActivity.this)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.receipt_line_remove_confirm)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            adapter.removeAt(position);
                            updateSummary();
                        })
                        .show();
            }

            @Override
            public void onItemChanged() {
                updateSummary();
            }
        });
        rv.setAdapter(adapter);
    }

    private void refreshAvailableProducts() {
        if (sanPhamDao == null) return;
        availableProducts = sanPhamDao.layTatCaChoAdmin();
        if (tvSupplierBrandHint != null) {
            tvSupplierBrandHint.setText(R.string.receipt_supplier_brand_hint);
            tvSupplierBrandHint.setVisibility(View.VISIBLE);
        }
    }

    private void showAddLineDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_receipt_line_form, null, false);
        Spinner spProduct = view.findViewById(R.id.spProduct);
        EditText edtQuantity = view.findViewById(R.id.edtReceiptLineQuantity);
        EditText edtUnitCost = view.findViewById(R.id.edtReceiptLineUnitCost);
        TextView tvEmptyMessage = view.findViewById(R.id.tvReceiptLineEmptyMessage);

        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected_admin, new ArrayList<>());
        productAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_admin);
        for (SanPham product : availableProducts) {
            productAdapter.add(product.tenSanPham);
        }
        if (spProduct != null) spProduct.setAdapter(productAdapter);

        boolean hasProducts = !availableProducts.isEmpty();
        if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(hasProducts ? View.GONE : View.VISIBLE);
        if (!hasProducts) {
            if (tvEmptyMessage != null) tvEmptyMessage.setText(R.string.receipt_no_products_available);
            if (spProduct != null) spProduct.setVisibility(View.GONE);
            if (edtQuantity != null) edtQuantity.setVisibility(View.GONE);
            if (edtUnitCost != null) edtUnitCost.setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.receipt_add_product)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, null)
                .create();

        dialog.setOnShowListener(d -> {
            if (!hasProducts) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                return;
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int selectedIndex = spProduct != null ? spProduct.getSelectedItemPosition() : -1;
                if (selectedIndex < 0 || selectedIndex >= availableProducts.size()) {
                    Toast.makeText(this, R.string.receipt_product_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = parsePositiveInt(edtQuantity != null ? edtQuantity.getText().toString().trim() : "");
                int unitCost = parsePositiveInt(edtUnitCost != null ? edtUnitCost.getText().toString().trim() : "");
                if (quantity <= 0) {
                    Toast.makeText(this, R.string.receipt_quantity_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (unitCost <= 0) {
                    Toast.makeText(this, R.string.receipt_cost_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                SanPham product = availableProducts.get(selectedIndex);
                PhieuNhapItem item = new PhieuNhapItem();
                item.productId = product.maSanPham;
                item.productName = product.tenSanPham;
                item.quantity = quantity;
                item.unitCost = unitCost;
                item.recalculateAmount();
                if (adapter != null) {
                    adapter.addOrMergeItem(item);
                }
                updateSummary();
                Toast.makeText(this, R.string.receipt_line_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void saveReceipt(boolean confirmReceipt) {
        if (!validateForm()) {
            return;
        }

        Spinner spSupplier = findViewById(R.id.spSupplier);
        EditText edtReceiptNote = findViewById(R.id.edtReceiptNote);
        if (spSupplier == null || edtReceiptNote == null) return;
        
        int supplierPos = spSupplier.getSelectedItemPosition();
        if (supplierPos < 0 || supplierPos >= suppliers.size()) {
            Toast.makeText(this, R.string.receipt_supplier_required, Toast.LENGTH_SHORT).show();
            return;
        }

        NhaCungCap supplier = suppliers.get(supplierPos);
        ArrayList<PhieuNhapItem> items = adapter.getData();
        long id = confirmReceipt
                ? phieuNhapDao.createConfirmedReceipt(supplier.id, edtReceiptNote.getText().toString().trim(), creatorName, items)
                : phieuNhapDao.saveDraftReceipt(supplier.id, edtReceiptNote.getText().toString().trim(), creatorName, items);

        if (id == -1) {
            Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        String receiptCode = PhieuNhap.formatCode(id);
        Toast.makeText(
                this,
                getString(confirmReceipt ? R.string.receipt_confirmed_with_code : R.string.receipt_saved_draft_with_code, receiptCode),
                Toast.LENGTH_SHORT
        ).show();
        setResult(RESULT_OK);
        finish();
    }

    private boolean validateForm() {
        if (suppliers.isEmpty()) {
            Toast.makeText(this, R.string.receipt_supplier_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (adapter == null || adapter.getData().isEmpty()) {
            Toast.makeText(this, R.string.receipt_lines_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        for (PhieuNhapItem item : adapter.getData()) {
            if (item.quantity <= 0) {
                Toast.makeText(this, R.string.receipt_quantity_required, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (item.unitCost <= 0) {
                Toast.makeText(this, R.string.receipt_cost_required, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void updateSummary() {
        if (adapter == null) return;
        int itemCount = adapter.getData().size();
        TextView tvLinesSummary = findViewById(R.id.tvReceiptLinesSummary);
        TextView tvTotalQty = findViewById(R.id.tvTotalQuantity);
        TextView tvTotalAmt = findViewById(R.id.tvTotalAmount);

        if (tvLinesSummary != null) {
            tvLinesSummary.setText(
                    itemCount == 0
                            ? getString(R.string.receipt_line_empty)
                            : getString(R.string.receipt_items_count, itemCount)
            );
        }
        if (tvTotalQty != null) tvTotalQty.setText(getString(R.string.receipt_quantity_value, adapter.getTotalQuantity()));
        if (tvTotalAmt != null) tvTotalAmt.setText(ReceiptUiFormatter.formatCurrency(this, adapter.getTotalAmount()));
    }


    private int parsePositiveInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String resolveCreatorName() {
        String username = new SessionManager(this).getUsername();
        if (username == null || username.trim().isEmpty()) {
            return getString(R.string.receipt_creator_default);
        }
        return username.trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
