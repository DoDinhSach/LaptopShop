package com.example.laptopshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.ui.products.ProductAdapter;
import com.example.laptopshop.ui.products.ProductsActivity;

import java.util.ArrayList;

public class CustomerHomeActivity extends BaseHomeActivity {

    private SanPhamDao sanPhamDao;
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final Handler flashSaleHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private Runnable flashSaleRunnable;
    private int currentBannerIndex = 0;
    private final int[] bannerImages = {
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3,
            R.drawable.banner4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redirect Admin to Admin Home if they accidentally land here
        if (isAdminSession()) {
            startActivity(new Intent(this, AdminHomeActivity.class));
            finish();
            return;
        }

        sanPhamDao = new SanPhamDao(this);
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_customer;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.content_home;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_customer;
    }

    @Override
    protected String screenTitle() {
        return "";
    }

    @Override
    protected boolean shouldShowToolbarActions() {
        return false;
    }

    @Override
    protected void onShellReady() {
        bindSearch();
        bindBanner();
        bindQuickCategories();
        bindSectionFeatured();
        bindSectionFlashSale();
        bindSectionSuggestions();
        startFlashSaleTimer();
    }

    private void bindSearch() {
        EditText edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            edtSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    performSearch(edtSearch.getText().toString().trim());
                    return true;
                }
                return false;
            });

            // Hỗ trợ bấm vào biểu tượng kính lúp để tìm kiếm
            edtSearch.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (edtSearch.getCompoundDrawables()[0] != null) {
                        if (event.getX() <= (edtSearch.getPaddingLeft() + edtSearch.getCompoundDrawables()[0].getBounds().width() + 30)) {
                            performSearch(edtSearch.getText().toString().trim());
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private void performSearch(String query) {
        if (!query.isEmpty()) {
            Intent intent = new Intent(this, ProductsActivity.class);
            intent.putExtra(ProductsActivity.EXTRA_QUERY, query);
            intent.putExtra(ProductsActivity.EXTRA_TITLE, "Kết quả tìm kiếm: " + query);
            startActivity(intent);
        }
    }

    private void bindBanner() {
        View btnBanner = findViewById(R.id.btnBannerBuyNow);
        ImageView ivBanner = findViewById(R.id.ivHomeBannerCurrent);
        
        if (ivBanner != null) {
            ivBanner.setImageResource(bannerImages[0]);
        }

        if (btnBanner != null) {
            btnBanner.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductsActivity.class);
                intent.putExtra(ProductsActivity.EXTRA_ONLY_DISCOUNTED, true);
                intent.putExtra(ProductsActivity.EXTRA_TITLE, "Flash Sale Siêu Rẻ");
                startActivity(intent);
            });
        }

        startBannerAutoSlide();
    }

    private void startBannerAutoSlide() {
        stopBannerAutoSlide();
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                currentBannerIndex = (currentBannerIndex + 1) % bannerImages.length;
                updateBannerImage();
                bannerHandler.postDelayed(this, 3000); // Đổi ảnh mỗi 3 giây
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void stopBannerAutoSlide() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void updateBannerImage() {
        ImageView ivBannerCurrent = findViewById(R.id.ivHomeBannerCurrent);
        ImageView ivBannerNext = findViewById(R.id.ivHomeBannerNext);

        if (ivBannerCurrent == null || ivBannerNext == null) return;

        // Thiết lập ảnh tiếp theo
        ivBannerNext.setImageResource(bannerImages[currentBannerIndex]);
        ivBannerNext.setAlpha(0f);
        ivBannerNext.setVisibility(View.VISIBLE);

        // Hiệu ứng Fade out ảnh cũ và Fade in ảnh mới
        ivBannerNext.animate()
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> {
                    ivBannerCurrent.setImageResource(bannerImages[currentBannerIndex]);
                    ivBannerNext.setVisibility(View.INVISIBLE);
                })
                .start();
    }

    private void bindQuickCategories() {
        setupCategory(R.id.tvBrandApple, "Apple");
        setupCategory(R.id.tvBrandDell, "Dell");
        setupCategory(R.id.tvBrandAsus, "Asus");
        setupCategory(R.id.tvBrandLenovo, "Lenovo");
        setupCategory(R.id.tvBrandHP, "HP");
        setupCategory(R.id.tvBrandMSI, "MSI");
    }

    private void setupCategory(int viewId, String brand) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductsActivity.class);
                intent.putExtra(ProductsActivity.EXTRA_BRAND, brand);
                startActivity(intent);
            });
        }
    }

    private void bindSectionFeatured() {
        RecyclerView rv = findViewById(R.id.rvFeaturedProducts);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ProductAdapter adapter = ProductAdapter.forCompactCarousel();
        rv.setAdapter(adapter);

        SanPhamDao.SanPhamFilter filter = new SanPhamDao.SanPhamFilter();
        filter.sortMode = "default";
        ArrayList<SanPham> products = sanPhamDao.locSanPham(filter);
        if (products.size() > 8) {
            products = new ArrayList<>(products.subList(0, 8));
        }
        adapter.setData(products);

        View btnViewAll = findViewById(R.id.tvViewAllPopular);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductsActivity.class);
                intent.putExtra(ProductsActivity.EXTRA_TITLE, "Laptop nổi bật");
                startActivity(intent);
            });
        }
    }

    private void bindSectionFlashSale() {
        RecyclerView rv = findViewById(R.id.rvFlashSale);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ProductAdapter adapter = ProductAdapter.forFlashSale();
        rv.setAdapter(adapter);

        SanPhamDao.SanPhamFilter filter = new SanPhamDao.SanPhamFilter();
        filter.onlyDiscounted = true;
        filter.sortMode = "discount_desc";
        ArrayList<SanPham> products = sanPhamDao.locSanPham(filter);
        if (products.size() > 10) {
            products = new ArrayList<>(products.subList(0, 10));
        }
        adapter.setData(products);

        View btnViewAll = findViewById(R.id.btnViewAllFlashSale);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductsActivity.class);
                intent.putExtra(ProductsActivity.EXTRA_ONLY_DISCOUNTED, true);
                intent.putExtra(ProductsActivity.EXTRA_TITLE, "Flash Sale");
                startActivity(intent);
            });
        }
    }

    private void bindSectionSuggestions() {
        RecyclerView rv = findViewById(R.id.rvSuggestedProducts);
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ProductAdapter adapter = ProductAdapter.forCompactCarousel();
        rv.setAdapter(adapter);

        SanPhamDao.SanPhamFilter filter = new SanPhamDao.SanPhamFilter();
        filter.onlyInStock = true;
        ArrayList<SanPham> products = sanPhamDao.locSanPham(filter);
        if (products.size() > 10) {
            products = new ArrayList<>(products.subList(0, 10));
        }
        adapter.setData(products);
    }

    private void startFlashSaleTimer() {
        stopFlashSaleTimer();
        flashSaleRunnable = new Runnable() {
            @Override
            public void run() {
                updateFlashSaleCountdown();
                flashSaleHandler.postDelayed(this, 1000);
            }
        };
        flashSaleHandler.post(flashSaleRunnable);
    }

    private void stopFlashSaleTimer() {
        if (flashSaleRunnable != null) {
            flashSaleHandler.removeCallbacks(flashSaleRunnable);
        }
    }

    private void updateFlashSaleCountdown() {
        TextView tvCountdown = findViewById(R.id.tvFlashSaleCountdown);
        if (tvCountdown == null) return;

        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar endOfDay = java.util.Calendar.getInstance();
        endOfDay.set(java.util.Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(java.util.Calendar.MINUTE, 59);
        endOfDay.set(java.util.Calendar.SECOND, 59);
        endOfDay.set(java.util.Calendar.MILLISECOND, 999);

        long diff = endOfDay.getTimeInMillis() - now.getTimeInMillis();
        if (diff <= 0) {
            // Reset sang ngày mới
            tvCountdown.setText("24:00:00");
            return;
        }

        long hours = diff / (60 * 60 * 1000);
        long minutes = (diff / (60 * 1000)) % 60;
        long seconds = (diff / 1000) % 60;

        String time = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        tvCountdown.setText(time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSections();
        startBannerAutoSlide();
        startFlashSaleTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoSlide();
        stopFlashSaleTimer();
    }

    private void refreshSections() {
        if (sanPhamDao != null) {
            bindSectionFeatured();
            bindSectionFlashSale();
            bindSectionSuggestions();
        }
    }
}
