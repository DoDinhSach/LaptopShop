package com.example.laptopshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminInventoryAlertsActivity extends BaseHomeActivity {

    private SanPhamDao sanPhamDao;
    private InventoryHistoryDao historyDao;
    private InventoryManagementAdapter adapter;
    private TextView tvAlertsHeaderCount;

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
        return R.layout.activity_admin_inventory_alerts;
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
        return getString(R.string.admin_alerts_title);
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
        tvAlertsHeaderCount = findViewById(R.id.tvAlertsHeaderCount);

        RecyclerView rv = findViewById(R.id.rvInventoryAlerts);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new InventoryManagementAdapter(item -> startActivity(new Intent(this, AdminReceiptEditorActivity.class)));
            rv.setAdapter(adapter);
        }

        loadData();
    }

    private void loadData() {
        if (sanPhamDao == null || historyDao == null) return;
        ArrayList<SanPham> products = sanPhamDao.layTatCa();
        HashMap<Long, int[]> totalsByProduct = InventoryDataHelper.buildHistoryTotals(historyDao.getAll());
        ArrayList<InventoryManagementItem> items = new ArrayList<>();

        for (SanPham product : products) {
            if (InventoryPolicy.isInStock(product.tonKho)) {
                continue;
            }
            items.add(InventoryDataHelper.toInventoryItem(this, product, totalsByProduct));
        }

        updateHeader(items.size());
        if (adapter != null) {
            adapter.setData(items);
        }
    }

    private void updateHeader(int count) {
        if (tvAlertsHeaderCount == null) return;
        if (count <= 0) {
            tvAlertsHeaderCount.setText(R.string.admin_alerts_header_empty);
            return;
        }
        tvAlertsHeaderCount.setText(getString(R.string.admin_alerts_summary_count, count));
    }
}
