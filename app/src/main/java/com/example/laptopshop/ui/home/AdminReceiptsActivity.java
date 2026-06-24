package com.example.laptopshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.PhieuNhapDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.PhieuNhap;
import com.example.laptopshop.ui.auth.WelcomeActivity;

import java.util.ArrayList;
import java.util.Locale;

public class AdminReceiptsActivity extends BaseHomeActivity {

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_DRAFT = PhieuNhap.STATUS_DRAFT;
    private static final String FILTER_COMPLETED = PhieuNhap.STATUS_COMPLETED;

    private PhieuNhapDao phieuNhapDao;
    private ReceiptAdapter adapter;
    private String currentFilter = FILTER_ALL;
    private TextView tvReceiptResultCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        phieuNhapDao = new PhieuNhapDao(this);
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.content_admin_receipts;
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
        return getString(R.string.admin_receipts_title);
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
        tvReceiptResultCount = findViewById(R.id.tvReceiptResultCount);

        RecyclerView rvReceipts = findViewById(R.id.rvReceipts);
        if (rvReceipts != null) {
            rvReceipts.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ReceiptAdapter(receipt -> {
                Intent intent = new Intent(this, AdminReceiptDetailActivity.class);
                intent.putExtra(AdminReceiptDetailActivity.EXTRA_RECEIPT_ID, receipt.id);
                startActivity(intent);
            });
            rvReceipts.setAdapter(adapter);
        }

        View btnAdd = findViewById(R.id.btnAddReceipt);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AdminReceiptEditorActivity.class)));
        
        setupFilters();

        EditText edtSearch = findViewById(R.id.edtReceiptSearch);
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
        if (phieuNhapDao != null) {
            EditText edtSearch = findViewById(R.id.edtReceiptSearch);
            loadData(edtSearch == null ? "" : edtSearch.getText().toString().trim());
        }
    }

    private void loadData(String keyword) {
        if (phieuNhapDao == null || adapter == null) return;
        ArrayList<PhieuNhap> receipts = phieuNhapDao.getRecentReceipts();
        ArrayList<PhieuNhap> filtered = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        for (PhieuNhap receipt : receipts) {
            if (!matchesFilter(receipt)) {
                continue;
            }
            if (!matchesKeyword(receipt, normalizedKeyword)) {
                continue;
            }
            filtered.add(receipt);
        }

        adapter.setData(filtered);
        if (tvReceiptResultCount != null) {
            tvReceiptResultCount.setText(getString(R.string.receipt_results_count, filtered.size()));
        }
    }

    private boolean matchesFilter(PhieuNhap receipt) {
        if (FILTER_ALL.equals(currentFilter)) {
            return true;
        }
        return currentFilter.equals(receipt.status);
    }

    private boolean matchesKeyword(PhieuNhap receipt, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }

        String code = receipt.getDisplayCode().toLowerCase(Locale.ROOT);
        String supplier = safeLower(receipt.supplierName);
        String note = safeLower(receipt.note);
        return code.contains(keyword) || supplier.contains(keyword) || note.contains(keyword);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private void setupFilters() {
        LinearLayout container = findViewById(R.id.layoutReceiptFilters);
        if (container == null) return;
        container.removeAllViews();
        addFilterChip(container, getString(R.string.filter_all), FILTER_ALL);
        addFilterChip(container, getString(R.string.receipt_status_draft), FILTER_DRAFT);
        addFilterChip(container, getString(R.string.receipt_status_completed), FILTER_COMPLETED);
    }

    private void addFilterChip(LinearLayout container, String label, String filter) {
        TextView chip = (TextView) LayoutInflater.from(this).inflate(R.layout.item_filter_chip, container, false);
        chip.setText(label);
        chip.setBackgroundResource(R.drawable.bg_chip_admin);
        chip.setSelected(filter.equals(currentFilter));
        chip.setTextColor(getColor(filter.equals(currentFilter) ? android.R.color.white : R.color.admin_text_secondary));
        chip.setOnClickListener(v -> {
            currentFilter = filter;
            setupFilters();
            EditText edtSearch = findViewById(R.id.edtReceiptSearch);
            loadData(edtSearch != null ? edtSearch.getText().toString().trim() : "");
        });
        container.addView(chip);
    }
}
