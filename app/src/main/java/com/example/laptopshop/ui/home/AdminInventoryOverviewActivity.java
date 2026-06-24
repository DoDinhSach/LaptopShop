package com.example.laptopshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.InventoryHistoryDao;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.utils.InventoryPolicy;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminInventoryOverviewActivity extends BaseHomeActivity {

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_IN_STOCK = InventoryManagementItem.STATUS_IN_STOCK;
    private static final String FILTER_LOW_STOCK = InventoryManagementItem.STATUS_LOW_STOCK;
    private static final String FILTER_OUT_OF_STOCK = InventoryManagementItem.STATUS_OUT_OF_STOCK;

    private SanPhamDao sanPhamDao;
    private InventoryHistoryDao historyDao;
    private InventoryManagementAdapter adapter;
    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        sanPhamDao = new SanPhamDao(this);
        historyDao = new InventoryHistoryDao(this);
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_admin_inventory_list;
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
        return getString(R.string.admin_inventory_overview_title);
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
        RecyclerView rv = findViewById(R.id.rvInventoryList);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new InventoryManagementAdapter();
            rv.setAdapter(adapter);
        }

        setupFilters();

        EditText edtSearch = findViewById(R.id.edtWarehouseSearch);
        if (edtSearch != null) {
            edtSearch.setHint(R.string.admin_inventory_search_hint);
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { loadData(s.toString().trim()); }
            });
        }

        loadData("");
    }

    private void loadData(String keyword) {
        if (sanPhamDao == null || historyDao == null || adapter == null) return;
        ArrayList<SanPham> products = keyword == null || keyword.isEmpty()
                ? sanPhamDao.layTatCa()
                : sanPhamDao.timKiem(keyword);
        HashMap<Long, int[]> totalsByProduct = InventoryDataHelper.buildHistoryTotals(historyDao.getAll());
        ArrayList<InventoryManagementItem> items = new ArrayList<>();

        for (SanPham product : products) {
            String status = InventoryPolicy.resolveStatus(product.tonKho);
            if (!matchesFilter(status)) {
                continue;
            }
            items.add(InventoryDataHelper.toInventoryItem(this, product, totalsByProduct));
        }

        adapter.setData(items);
    }

    private boolean matchesFilter(String status) {
        return FILTER_ALL.equals(currentFilter) || currentFilter.equals(status);
    }

    private void setupFilters() {
        LinearLayout container = findViewById(R.id.layoutWarehouseFilters);
        if (container == null) return;
        container.removeAllViews();
        addFilterChip(container, getString(R.string.filter_all), FILTER_ALL);
        addFilterChip(container, getString(R.string.admin_filter_in_stock), FILTER_IN_STOCK);
        addFilterChip(container, getString(R.string.filter_low_stock), FILTER_LOW_STOCK);
        addFilterChip(container, getString(R.string.filter_out_of_stock), FILTER_OUT_OF_STOCK);
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
            EditText edtSearch = findViewById(R.id.edtWarehouseSearch);
            loadData(edtSearch != null ? edtSearch.getText().toString().trim() : "");
        });
        container.addView(chip);
    }
}
