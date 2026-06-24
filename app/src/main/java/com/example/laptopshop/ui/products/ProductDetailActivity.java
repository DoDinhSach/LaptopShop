package com.example.laptopshop.ui.products;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.GioHangDao;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.ui.auth.LoginActivity;
import com.example.laptopshop.ui.cart.CartActivity;
import com.example.laptopshop.ui.checkout.CheckoutActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;
import com.example.laptopshop.utils.InventoryPolicy;
import com.example.laptopshop.utils.ProductImageLoader;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends BaseHomeActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private SanPhamDao sanPhamDao;
    private GioHangDao gioHangDao;

    private SanPham product;
    private int qty = 1;
    private TextView tvCartBadge;

    private String selectedStorage = "";
    private String selectedColor = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sanPhamDao = new SanPhamDao(this);
        gioHangDao = new GioHangDao(this);

        long id = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        product = sanPhamDao.getById(id);
        if (product == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView iv = findViewById(R.id.ivImage);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvBrand = findViewById(R.id.tvBrand);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvPriceOriginal = findViewById(R.id.tvPriceOriginal);
        TextView tvDiscountBadge = findViewById(R.id.tvDiscountBadge);
        TextView tvStock = findViewById(R.id.tvStock);
        TextView tvDesc = findViewById(R.id.tvDesc);
        TextView tvQty = findViewById(R.id.tvQty);
        TextView tvRatingValue = findViewById(R.id.tvRatingValue);
        TextView tvRatingStars = findViewById(R.id.tvRatingStars);
        TextView tvRatingMeta = findViewById(R.id.tvRatingMeta);
        TextView tvSpecScreen = findViewById(R.id.tvSpecScreen);
        TextView tvSpecCamera = findViewById(R.id.tvSpecCamera);
        TextView tvSpecRam = findViewById(R.id.tvSpecRam);
        TextView tvSpecPin = findViewById(R.id.tvSpecPin);

        LinearLayout layoutStorageOptions = findViewById(R.id.layoutStorageOptions);
        LinearLayout layoutColorOptions = findViewById(R.id.layoutColorOptions);

        MaterialButton btnMinus = findViewById(R.id.btnMinus);
        MaterialButton btnPlus = findViewById(R.id.btnPlus);
        MaterialButton btnAdd = findViewById(R.id.btnAddToCart);
        MaterialButton btnBuyNow = findViewById(R.id.btnBuyNow);

        if (iv != null) ProductImageLoader.load(iv, product.tenAnh, product.tenSanPham, product.hang);

        String brand = (product.hang == null || product.hang.trim().isEmpty()) ? "LaptopShop" : product.hang.trim();
        if (tvName != null) tvName.setText(product.tenSanPham);
        if (tvBrand != null) tvBrand.setText(brand);

        int discountPercent = Math.max(0, Math.min(100, product.giamGia));
        int originalPrice = Math.max(0, product.gia);
        int discountedPrice = (int) ((long) originalPrice * (100 - discountPercent) / 100);

        if (tvPrice != null) tvPrice.setText(formatMoney(discountedPrice));
        if (tvPriceOriginal != null) {
            tvPriceOriginal.setText(getString(R.string.product_original_price_label, formatMoney(originalPrice)));
            tvPriceOriginal.setPaintFlags(tvPriceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (tvDiscountBadge != null) {
            tvDiscountBadge.setText(getString(R.string.product_discount_short, discountPercent));
            tvDiscountBadge.setVisibility(discountPercent > 0 ? View.VISIBLE : View.GONE);
        }

        float rating = buildRating(product);
        int ratingCount = buildRatingCount(product);
        if (tvRatingValue != null) tvRatingValue.setText(String.format(Locale.US, "%.1f", rating));
        if (tvRatingStars != null) tvRatingStars.setText(buildStarText(rating));
        if (tvRatingMeta != null) tvRatingMeta.setText(getString(R.string.product_rating_count, ratingCount));

        if (tvStock != null) {
            tvStock.setText(InventoryPolicy.getCustomerStockText(this, product.tonKho));
            tvStock.setTextColor(ContextCompat.getColor(this,
                    InventoryPolicy.isInStock(product.tonKho) ? R.color.text_sub : R.color.red_primary));
        }
        if (tvDesc != null) tvDesc.setText(product.moTa == null ? "" : product.moTa);

        if (layoutStorageOptions != null) populateOptionChips(layoutStorageOptions, buildStorageOptions(product), true);
        if (layoutColorOptions != null) populateOptionChips(layoutColorOptions, buildColorOptions(product), false);

        if (tvSpecScreen != null) tvSpecScreen.setText(getString(R.string.product_spec_screen, safeValue(product.manHinh, getString(R.string.product_not_updated))));
        if (tvSpecCamera != null) tvSpecCamera.setText(getString(R.string.product_spec_camera, safeValue(product.camera, getString(R.string.product_not_updated))));
        if (tvSpecRam != null) tvSpecRam.setText(buildRamChipsetLine(product));
        if (tvSpecPin != null) tvSpecPin.setText(buildBatteryOsLine(product));

        if (tvQty != null) tvQty.setText(String.valueOf(qty));

        if (btnMinus != null) btnMinus.setOnClickListener(v -> {
            if (qty > 1) qty--;
            if (tvQty != null) tvQty.setText(String.valueOf(qty));
        });

        if (btnPlus != null) btnPlus.setOnClickListener(v -> {
            if (qty < product.tonKho) qty++;
            if (tvQty != null) tvQty.setText(String.valueOf(qty));
        });

        if (btnAdd != null) btnAdd.setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            if (!addToCart()) return;
            refreshCartBadge();
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });

        if (btnBuyNow != null) btnBuyNow.setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            if (product.tonKho <= 0) {
                Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, CheckoutActivity.class);
            i.putExtra(CheckoutActivity.EXTRA_BUY_NOW_PRODUCT_ID, product.maSanPham);
            i.putExtra(CheckoutActivity.EXTRA_BUY_NOW_QTY, qty);
            i.putExtra(CheckoutActivity.EXTRA_BUY_NOW_STORAGE, selectedStorage);
            i.putExtra(CheckoutActivity.EXTRA_BUY_NOW_COLOR, selectedColor);
            startActivity(i);
        });
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_product_detail;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_customer;
    }

    @Override
    protected String screenTitle() {
        return getString(R.string.product_detail_title);
    }

    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_products;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return true;
    }

    @Override
    protected boolean isBottomNavRootScreen() {
        return false;
    }

    @Override
    protected int toolbarMenuRes() {
        return R.menu.menu_product_detail_actions;
    }

    @Override
    protected void onShellReady() {
        super.onShellReady();
        View contentContainer = findViewById(R.id.homeContentContainer);
        if (contentContainer != null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentContainer.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.header_red_height);
            contentContainer.setLayoutParams(params);
        }
    }

    private String formatMoney(int value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + "đ";
    }

    private float buildRating(SanPham p) {
        String key = (p.tenSanPham + " " + p.hang).toLowerCase(Locale.ROOT);
        if (key.contains("macbook") || key.contains("apple")) return 4.9f;
        if (key.contains("xps") || key.contains("dell")) return 4.8f;
        if (key.contains("zenbook") || key.contains("asus")) return 4.7f;
        if (key.contains("thinkpad") || key.contains("lenovo")) return 4.8f;
        return 4.5f;
    }

    private int buildRatingCount(SanPham p) {
        String key = (p.tenSanPham + " " + p.hang).toLowerCase(Locale.ROOT);
        if (key.contains("macbook") || key.contains("apple")) return 328;
        if (key.contains("xps") || key.contains("dell")) return 241;
        if (key.contains("zenbook") || key.contains("asus")) return 189;
        return 120;
    }

    private String buildStarText(float rating) {
        int fullStars = Math.round(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < fullStars ? "★" : "☆");
        }
        return stars.toString();
    }

    private void populateOptionChips(LinearLayout container, List<String> options, boolean isStorage) {
        container.removeAllViews();
        for (int i = 0; i < options.size(); i++) {
            final String value = options.get(i);

            TextView chip = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (i < options.size() - 1) lp.rightMargin = dp(8);
            chip.setLayoutParams(lp);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            chip.setBackgroundResource(R.drawable.bg_option_chip);
            chip.setTextColor(ContextCompat.getColorStateList(this, R.color.option_chip_text));
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setText(value);
            chip.setClickable(true);
            chip.setFocusable(true);

            chip.setOnClickListener(v -> {
                clearSelectedState(container);
                chip.setSelected(true);
                if (isStorage) {
                    selectedStorage = value;
                } else {
                    selectedColor = value;
                }
            });

            container.addView(chip);

            if (i == 0) {
                chip.setSelected(true);
                if (isStorage) {
                    selectedStorage = value;
                } else {
                    selectedColor = value;
                }
            }
        }
    }

    private void clearSelectedState(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setSelected(false);
        }
    }

    private List<String> buildStorageOptions(SanPham p) {
        ArrayList<String> options = new ArrayList<>();
        if (p.romGb > 0) {
            options.add(p.romGb + "GB");
        }
        if (options.isEmpty()) {
            options.add("256GB");
        }
        return options;
    }

    private List<String> buildColorOptions(SanPham p) {
        ArrayList<String> options = new ArrayList<>();
        if (p.mauSac != null) {
            String[] rawColors = p.mauSac.split(",");
            for (String color : rawColors) {
                String trimmed = color.trim();
                if (!trimmed.isEmpty()) {
                    options.add(trimmed);
                }
            }
        }
        if (options.isEmpty()) {
            options.add("Bạc");
        }
        return options;
    }

    private String buildRamChipsetLine(SanPham p) {
        String ramText = p.ramGb > 0 ? p.ramGb + "GB" : getString(R.string.product_not_updated);
        String chipsetText = safeValue(p.chipset, getString(R.string.product_not_updated));
        return getString(R.string.product_spec_ram_chip, ramText, chipsetText);
    }

    private String buildBatteryOsLine(SanPham p) {
        String batteryText = p.pinMah > 0
                ? NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(p.pinMah) + "Wh"
                : getString(R.string.product_not_updated);
        String osText = safeValue(p.heDieuHanh, getString(R.string.product_not_updated));
        return getString(R.string.product_spec_battery_os, batteryText, osText);
    }

    private String safeValue(String value, String fallback) {
        return TextUtils.isEmpty(value == null ? null : value.trim()) ? fallback : value.trim();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private boolean ensureLoggedIn() {
        if (session.isLoggedIn()) return true;
        startActivity(new Intent(this, LoginActivity.class));
        Toast.makeText(this, "Vui lòng đăng nhập/đăng ký để tiếp tục", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean addToCart() {
        if (product.tonKho <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean ok = gioHangDao.addOrIncrease(session.getUserId(), product.maSanPham, qty, selectedStorage, selectedColor);
        if (!ok) {
            Toast.makeText(this, "Không thể thêm (vượt tồn kho hoặc sản phẩm đã ngừng bán)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean created = super.onCreateOptionsMenu(menu);
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        if (cartItem == null) return created;

        View actionView = cartItem.getActionView();
        if (actionView != null) {
            tvCartBadge = actionView.findViewById(R.id.tvCartBadge);
            actionView.setOnClickListener(v -> openCartFromToolbar());
            refreshCartBadge();
        }
        return created;
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
        if (!ensureLoggedIn()) return;
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
}
