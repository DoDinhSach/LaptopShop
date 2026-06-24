package com.example.laptopshop.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.GioHangDao;
import com.example.laptopshop.data.dao.GiamGiaDao;
import com.example.laptopshop.data.model.GioHangItem;
import com.example.laptopshop.data.model.CheckoutInfo;
import com.example.laptopshop.data.model.GiamGia;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.ui.checkout.CheckoutActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CartActivity extends BaseHomeActivity {

    public static final String EXTRA_SELECTED_CART_ITEM_IDS = "extra_selected_cart_item_ids";
    public static final String EXTRA_SELECTED_PRODUCT_IDS = EXTRA_SELECTED_CART_ITEM_IDS; 
    public static final String EXTRA_DISCOUNT_CODE = "extra_discount_code";

    private GioHangDao gioHangDao;
    private GiamGiaDao giamGiaDao;

    private CartAdapter adapter;
    private TextView tvTotal;
    private TextView tvSubtotal;
    private TextView tvShippingFee;
    private TextView tvDiscountAmount;
    private TextView tvDiscountMessage;
    private EditText edtDiscountCode;

    private final Set<Long> selectedCartItemIds = new HashSet<>();
    private boolean didInitSelectAll = false;
    private String appliedDiscountCode;
    private GiamGia currentVoucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!session.isLoggedIn() || session.getUserId() <= 0) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        gioHangDao = new GioHangDao(this);
        giamGiaDao = new GiamGiaDao(this);

        tvTotal = findViewById(R.id.tvTotal);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount);
        tvDiscountMessage = findViewById(R.id.tvDiscountMessage);
        edtDiscountCode = findViewById(R.id.edtDiscountCode);

        RecyclerView rv = findViewById(R.id.rvCart);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CartAdapter(new CartAdapter.Listener() {
                @Override
                public void onChangeQty(GioHangItem item, int newQty) {
                    boolean ok = gioHangDao.updateQtyById(session.getUserId(), item.id, newQty);
                    if (!ok) Toast.makeText(CartActivity.this, R.string.exceed_stock, Toast.LENGTH_SHORT).show();
                    reload();
                }

                @Override
                public void onRemove(GioHangItem item) {
                    gioHangDao.deleteItemById(session.getUserId(), item.id);
                    selectedCartItemIds.remove(item.id);
                    reload();
                }

                @Override
                public void onToggleSelection(GioHangItem item, boolean isSelected) {
                    if (isSelected) {
                        selectedCartItemIds.add(item.id);
                    } else {
                        selectedCartItemIds.remove(item.id);
                    }
                    updateSelectedTotal();
                }
            });
            rv.setAdapter(adapter);
        }

        MaterialButton btnApplyDiscount = findViewById(R.id.btnApplyDiscount);
        if (btnApplyDiscount != null) {
            btnApplyDiscount.setOnClickListener(v -> applyDiscountCode());
        }

        MaterialButton btnBuy = findViewById(R.id.btnBuyNow);
        if (btnBuy != null) {
            btnBuy.setOnClickListener(v -> openCheckout());
        }

        reload();
    }

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_customer;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_cart;
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
    protected int selectedBottomNavItemId() {
        return R.id.nav_cart;
    }

    @Override
    protected boolean shouldShowToolbarActions() {
        return false;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return false;
    }

    private void reload() {
        ArrayList<GioHangItem> list = gioHangDao.getCartItems(session.getUserId());

        Set<Long> availableIds = new HashSet<>();
        for (GioHangItem item : list) availableIds.add(item.id);
        selectedCartItemIds.retainAll(availableIds);

        if (!didInitSelectAll) {
            selectedCartItemIds.addAll(availableIds);
            didInitSelectAll = true;
        }

        if (adapter != null) {
            adapter.setData(list);
            adapter.setSelectedCartItemIds(selectedCartItemIds);
        }
        updateSelectedTotal();
    }

    private void applyDiscountCode() {
        if (edtDiscountCode == null) return;
        String rawCode = edtDiscountCode.getText().toString();
        int subtotal = gioHangDao.getTotalByIds(session.getUserId(), new ArrayList<>(selectedCartItemIds));
        String normalizedCode = CheckoutInfo.normalizeDiscountCode(rawCode);

        if (normalizedCode == null) {
            appliedDiscountCode = null;
            currentVoucher = null;
            if (tvDiscountMessage != null) tvDiscountMessage.setVisibility(android.view.View.GONE);
            updateSelectedTotal();
            return;
        }

        GiamGia voucher = giamGiaDao.getByCode(normalizedCode);
        int discount = CheckoutInfo.calculateDiscount(voucher, subtotal);

        if (discount <= 0) {
            appliedDiscountCode = null;
            currentVoucher = null;
            if (tvDiscountMessage != null) {
                tvDiscountMessage.setVisibility(android.view.View.VISIBLE);
                tvDiscountMessage.setText(R.string.discount_invalid_message);
            }
            updateSelectedTotal();
            return;
        }

        appliedDiscountCode = normalizedCode;
        currentVoucher = voucher;
        edtDiscountCode.setText(normalizedCode);
        edtDiscountCode.setSelection(normalizedCode.length());
        if (tvDiscountMessage != null) {
            tvDiscountMessage.setVisibility(android.view.View.VISIBLE);
            tvDiscountMessage.setText(getString(R.string.discount_applied_message, normalizedCode, formatMoney(discount)));
        }
        updateSelectedTotal();
    }

    private void updateSelectedTotal() {
        int subtotal = gioHangDao.getTotalByIds(session.getUserId(), new ArrayList<>(selectedCartItemIds));
        int discount = CheckoutInfo.calculateDiscount(currentVoucher, subtotal);
        int shippingFee = subtotal > 0 ? CheckoutInfo.SHIPPING_FEE : 0;
        int finalTotal = Math.max(0, subtotal + shippingFee - discount);

        if (tvSubtotal != null) tvSubtotal.setText(formatMoney(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(formatMoney(shippingFee));
        if (tvDiscountAmount != null) tvDiscountAmount.setText(formatMoney(discount));
        if (tvTotal != null) tvTotal.setText(getString(R.string.order_total_summary, formatMoney(finalTotal)));
    }

    private String formatMoney(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private void openCheckout() {
        if (selectedCartItemIds.isEmpty()) {
            Toast.makeText(this, R.string.cart_select_at_least_one, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, CheckoutActivity.class);
        i.putExtra(EXTRA_SELECTED_CART_ITEM_IDS, toLongArray(selectedCartItemIds));
        i.putExtra(EXTRA_DISCOUNT_CODE, appliedDiscountCode);
        startActivity(i);
    }

    private long[] toLongArray(Set<Long> ids) {
        long[] arr = new long[ids.size()];
        int i = 0;
        for (Long id : ids) {
            arr[i++] = id;
        }
        return arr;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gioHangDao != null) {
            reload();
        }
    }
}
