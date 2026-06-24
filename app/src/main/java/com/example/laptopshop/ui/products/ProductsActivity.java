package com.example.laptopshop.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.GioHangDao;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.ui.auth.LoginActivity;
import com.example.laptopshop.ui.cart.CartActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;

import java.util.ArrayList;

public class ProductsActivity extends BaseHomeActivity {

    public static final String EXTRA_BRAND = "extra_brand";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_QUERY = "extra_query";
    public static final String EXTRA_ONLY_DISCOUNTED = "extra_only_discounted"; 

    private static final String SORT_DEFAULT = "default";
    private static final String SORT_PRICE_ASC = "price_asc";
    private static final String SORT_PRICE_DESC = "price_desc";
    private static final String SORT_NAME_ASC = "name_asc";
    private static final String SORT_DISCOUNT_DESC = "discount_desc";

    private SanPhamDao sanPhamDao;
    private GioHangDao gioHangDao;
    private ProductAdapter adapter;

    private RecyclerView rvProducts;
    private EditText edtSearch;
    private TextView tvResultCount;
    private TextView tvCartBadge;
    private LinearLayout layoutBrandFilters;
    private View layoutEmpty;
    private Spinner spSort;
    private Spinner spPrice;
    private Spinner spType;
    private Spinner spScreenSize;
    private Spinner spCpu;
    private Spinner spRam;
    private Spinner spSsd;
    private Spinner spRefreshRate;
    private Spinner spResolution;
    private TextView chipInStock;
    private TextView chipDiscounted;

    private final SanPhamDao.SanPhamFilter filter = new SanPhamDao.SanPhamFilter();
    private final ArrayList<String> brands = new ArrayList<>();
    private String toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        filter.brand = trimToNull(getIntent().getStringExtra(EXTRA_BRAND));
        String title = trimToNull(getIntent().getStringExtra(EXTRA_TITLE));
        toolbarTitle = title != null ? title : filter.brand;

        super.onCreate(savedInstanceState);

        sanPhamDao = new SanPhamDao(this);
        gioHangDao = new GioHangDao(this);

        rvProducts = findViewById(R.id.rvProducts);
        if (rvProducts != null) {
            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = ProductAdapter.forCatalogCompact();
            rvProducts.setAdapter(adapter);
        }

        edtSearch = findViewById(R.id.edtSearch);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutBrandFilters = findViewById(R.id.layoutBrandFilters);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        spSort = findViewById(R.id.spSort);
        spPrice = findViewById(R.id.spPrice);
        spType = findViewById(R.id.spType);
        spScreenSize = findViewById(R.id.spScreenSize);
        spCpu = findViewById(R.id.spCpu);
        spRam = findViewById(R.id.spRam);
        spSsd = findViewById(R.id.spSsd);
        spRefreshRate = findViewById(R.id.spRefreshRate);
        spResolution = findViewById(R.id.spResolution);
        chipInStock = findViewById(getResources().getIdentifier("chipInStock", "id", getPackageName()));
        chipDiscounted = findViewById(getResources().getIdentifier("chipDiscounted", "id", getPackageName()));

        String initKey = getIntent().getStringExtra(EXTRA_QUERY);
        if (initKey == null) initKey = "";
        initKey = initKey.trim();
        filter.keyword = trimToNull(initKey);
        boolean openDiscountedOnly = getIntent().getBooleanExtra(EXTRA_ONLY_DISCOUNTED, false);
        filter.onlyDiscounted = openDiscountedOnly;
        filter.sortMode = openDiscountedOnly ? SORT_DISCOUNT_DESC : SORT_DEFAULT;

        if (edtSearch != null) {
            edtSearch.setText(initKey);
            edtSearch.setSelection(edtSearch.getText().length());
        }

        setupBrandFilters();
        setupSpinners();
        setupToggles();
        
        if (chipInStock != null) {
            chipInStock.setTextColor(getColor(R.color.text_primary));
        }
        if (chipDiscounted != null) {
            chipDiscounted.setSelected(filter.onlyDiscounted);
            chipDiscounted.setTextColor(getColor(filter.onlyDiscounted ? R.color.red_primary : R.color.text_primary));
        }
        bindSearch();

        View btnClearFilters = findViewById(R.id.btnClearFilters);
        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> clearAllFilters());
        }

        loadData();
    }

    private void clearAllFilters() {
        filter.keyword = null;
        filter.brand = null;
        filter.productType = null;
        filter.chipset = null;
        filter.screenSize = null;
        filter.resolution = null;
        filter.minPrice = 0;
        filter.maxPrice = 0;
        filter.minRomGb = 0;
        filter.maxRomGb = 0;
        filter.minRamGb = 0;
        filter.maxRamGb = 0;
        filter.refreshRate = 0;
        
        if (edtSearch != null) edtSearch.setText("");
        if (spType != null) spType.setSelection(0);
        if (spPrice != null) spPrice.setSelection(0);
        if (spScreenSize != null) spScreenSize.setSelection(0);
        if (spSort != null) spSort.setSelection(0);
        if (spCpu != null) spCpu.setSelection(0);
        if (spRam != null) spRam.setSelection(0);
        if (spSsd != null) spSsd.setSelection(0);
        if (spRefreshRate != null) spRefreshRate.setSelection(0);
        if (spResolution != null) spResolution.setSelection(0);
        
        renderBrandFilters();
        loadData();
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_customer;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_products;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_customer;
    }

    @Override
    protected String screenTitle() {
        return toolbarTitle == null ? getString(R.string.nav_products_label) : getString(R.string.products_title_with_context, toolbarTitle);
    }

    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_products;
    }

    @Override
    protected boolean shouldShowToolbarActions() {
        return false;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return false;
    }

    @Override
    protected int toolbarMenuRes() {
        return R.menu.menu_product_detail_actions;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            openCartFromToolbar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBadge();
    }

    private void openCartFromToolbar() {
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        startActivity(new Intent(this, CartActivity.class));
    }

    private void refreshCartBadge() {
        if (tvCartBadge == null || gioHangDao == null) return;
        if (!session.isLoggedIn()) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        int totalQty = gioHangDao.getTotalQty(session.getUserId());
        if (totalQty <= 0) {
            tvCartBadge.setVisibility(View.GONE);
            return;
        }

        tvCartBadge.setText(totalQty > 99 ? "99+" : String.valueOf(totalQty));
        tvCartBadge.setVisibility(View.VISIBLE);
    }

    private void bindSearch() {
        if (edtSearch == null) return;
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filter.keyword = trimToNull(s.toString());
                loadData();
            }
        });

        // Hỗ trợ bấm vào biểu tượng kính lúp để tìm kiếm ngay lập tức (dù đã có TextWatcher)
        edtSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (edtSearch.getCompoundDrawables()[0] != null) {
                    if (event.getX() <= (edtSearch.getPaddingLeft() + edtSearch.getCompoundDrawables()[0].getBounds().width() + 30)) {
                        filter.keyword = trimToNull(edtSearch.getText().toString());
                        loadData();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void setupBrandFilters() {
        // Redundant as we now have a Brand Spinner, but keeping empty for now to avoid crashes if called
        brands.clear();
        if (layoutBrandFilters != null) layoutBrandFilters.setVisibility(View.GONE);
    }

    private void renderBrandFilters() {
        if (layoutBrandFilters == null) return;
        layoutBrandFilters.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        String allLabel = getString(R.string.filter_all);
        for (String brand : brands) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_filter_chip, layoutBrandFilters, false);
            chip.setText(brand);
            
            // Adjust chip padding to be smaller
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            
            boolean isAll = allLabel.equals(brand);
            boolean selected = isAll ? filter.brand == null : brand.equalsIgnoreCase(filter.brand == null ? "" : filter.brand);
            chip.setSelected(selected);
            chip.setTextColor(getColor(selected ? R.color.red_primary : R.color.text_primary));
            chip.setOnClickListener(v -> {
                filter.brand = isAll ? null : brand;
                renderBrandFilters();
                loadData();
            });
            layoutBrandFilters.addView(chip);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setupSpinners() {
        if (spSort != null) {
            bindSpinner(spSort,
                    new String[]{"Sắp xếp", "Mặc định", "Giá từ thấp lên cao", "Giá từ cao xuống thấp", "Tên A-Z", "Giảm giá cao"},
                    position -> {
                        if (position == 2) filter.sortMode = SORT_PRICE_ASC;
                        else if (position == 3) filter.sortMode = SORT_PRICE_DESC;
                        else if (position == 4) filter.sortMode = SORT_NAME_ASC;
                        else if (position == 5) filter.sortMode = SORT_DISCOUNT_DESC;
                        else filter.sortMode = SORT_DEFAULT;
                        loadData();
                    });
        }

        if (spPrice != null) {
            bindSpinner(spPrice,
                    new String[]{"Tất cả giá", "Dưới 10 triệu", "10 - 20 triệu", "20 - 30 triệu", "Trên 30 triệu"},
                    position -> {
                        filter.minPrice = 0;
                        filter.maxPrice = 0;
                        if (position == 1) {
                            filter.maxPrice = 10_000_000;
                        } else if (position == 2) {
                            filter.minPrice = 10_000_000;
                            filter.maxPrice = 20_000_000;
                        } else if (position == 3) {
                            filter.minPrice = 20_000_000;
                            filter.maxPrice = 30_000_000;
                        } else if (position == 4) {
                            filter.minPrice = 30_000_000;
                        }
                        loadData();
                    });
        }

        if (spType != null) {
            bindSpinner(spType,
                    new String[]{"Thương hiệu", "Apple", "Dell", "Asus", "Lenovo", "HP", "MSI"},
                    position -> {
                        if (position == 0) filter.brand = null;
                        else filter.brand = (String) spType.getItemAtPosition(position);
                        loadData();
                    });
        }

        if (spScreenSize != null) {
            bindSpinner(spScreenSize,
                    new String[]{"Màn hình", "Dưới 14 inch", "14 inch", "15.6 inch", "16 inch trở lên"},
                    position -> {
                        if (position == 1) filter.screenSize = "13.";
                        else if (position == 2) filter.screenSize = "14 inch";
                        else if (position == 3) filter.screenSize = "15.6";
                        else if (position == 4) filter.screenSize = "16";
                        else filter.screenSize = null;
                        loadData();
                    });
        }

        if (spCpu != null) {
            bindSpinner(spCpu,
                    new String[]{"CPU", "Intel Core i3", "Intel Core i5", "Intel Core i7", "Intel Core i9", "Intel Core Ultra 5", "Intel Core Ultra 7", "AMD Ryzen 7", "AMD Ryzen 9", "Apple M1", "Apple M2", "Apple M3", "Apple M4"},
                    position -> {
                        if (position == 0) filter.chipset = null;
                        else filter.chipset = (String) spCpu.getItemAtPosition(position);
                        loadData();
                    });
        }

        if (spRam != null) {
            bindSpinner(spRam,
                    new String[]{"RAM", "8GB", "16GB", "32GB", "64GB"},
                    position -> {
                        filter.minRamGb = 0;
                        filter.maxRamGb = 0;
                        if (position == 1) { filter.minRamGb = 8; filter.maxRamGb = 8; }
                        else if (position == 2) { filter.minRamGb = 16; filter.maxRamGb = 16; }
                        else if (position == 3) { filter.minRamGb = 32; filter.maxRamGb = 32; }
                        else if (position == 4) { filter.minRamGb = 64; filter.maxRamGb = 64; }
                        loadData();
                    });
        }

        if (spSsd != null) {
            bindSpinner(spSsd,
                    new String[]{"SSD", "256GB", "512GB", "1TB", "2TB"},
                    position -> {
                        filter.minRomGb = 0;
                        filter.maxRomGb = 0;
                        if (position == 1) { filter.minRomGb = 256; filter.maxRomGb = 256; }
                        else if (position == 2) { filter.minRomGb = 512; filter.maxRomGb = 512; }
                        else if (position == 3) { filter.minRomGb = 1024; filter.maxRomGb = 1024; }
                        else if (position == 4) { filter.minRomGb = 2048; filter.maxRomGb = 2048; }
                        loadData();
                    });
        }

        if (spRefreshRate != null) {
            bindSpinner(spRefreshRate,
                    new String[]{"Tần số quét", "60Hz", "90Hz", "120Hz", "165Hz"},
                    position -> {
                        if (position == 1) filter.refreshRate = 60;
                        else if (position == 2) filter.refreshRate = 90;
                        else if (position == 3) filter.refreshRate = 120;
                        else if (position == 4) filter.refreshRate = 165;
                        else filter.refreshRate = 0;
                        loadData();
                    });
        }

        if (spResolution != null) {
            bindSpinner(spResolution,
                    new String[]{"Độ phân giải", "HD", "Full HD", "2K", "2.8K", "3K", "4K"},
                    position -> {
                        if (position == 0) filter.resolution = null;
                        else if (position == 1) filter.resolution = "1366";
                        else if (position == 2) filter.resolution = "1920";
                        else if (position == 3) filter.resolution = "2560";
                        else if (position == 4) filter.resolution = "2.8K";
                        else if (position == 5) filter.resolution = "3K";
                        else if (position == 6) filter.resolution = "3840";
                        else filter.resolution = null;
                        loadData();
                    });
        }
    }

    private void bindSpinner(Spinner spinner, String[] items, OnSpinnerChanged listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, items);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupToggles() {
        if (chipInStock != null) {
            chipInStock.setOnClickListener(v -> {
                chipInStock.setSelected(!chipInStock.isSelected());
                chipInStock.setTextColor(getColor(chipInStock.isSelected() ? R.color.red_primary : R.color.text_primary));
                filter.onlyInStock = chipInStock.isSelected();
                loadData();
            });
        } else {
            filter.onlyInStock = false;
        }

        if (chipDiscounted != null) {
            chipDiscounted.setOnClickListener(v -> {
                chipDiscounted.setSelected(!chipDiscounted.isSelected());
                chipDiscounted.setTextColor(getColor(chipDiscounted.isSelected() ? R.color.red_primary : R.color.text_primary));
                filter.onlyDiscounted = chipDiscounted.isSelected();
                loadData();
            });
        } else {
            filter.onlyDiscounted = false;
        }
    }

    private void loadData() {
        if (sanPhamDao == null) return;
        ArrayList<SanPham> products = sanPhamDao.locSanPham(filter);
        if (adapter != null) {
            adapter.setData(products);
        }
        
        String countText = products.size() == 0 
                ? "Không tìm thấy sản phẩm nào" 
                : getString(R.string.products_found_count, products.size());
        if (tvResultCount != null) tvResultCount.setText(countText);
        
        if (products.isEmpty()) {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            if (rvProducts != null) rvProducts.setVisibility(View.GONE);
        } else {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private interface OnSpinnerChanged {
        void onChanged(int position);
    }
}
