package com.example.laptopshop.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.NguoiDungDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.NguoiDung;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminCustomersActivity extends BaseHomeActivity {
    private NguoiDungDao nguoiDungDao;
    private AdminCustomersAdapter adapter;
    private EditText edtSearch;
    private TextView tvCustomersCount;
    private TextView tvCustomersFiltered;
    private TextView tvCustomerResultCount;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.content_admin_customers;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_admin;
    }

    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_admin_customers;
    }

    @Override
    protected String screenTitle() {
        return getString(R.string.admin_customers_title);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        nguoiDungDao = new NguoiDungDao(this);

        edtSearch = findViewById(R.id.edtSearch);
        View cardCustomersCount = findViewById(R.id.cardCustomersCount);
        View cardCustomersFiltered = findViewById(R.id.cardCustomersFiltered);
        if (cardCustomersCount != null) {
            ((TextView) cardCustomersCount.findViewById(R.id.tvKpiLabel)).setText(R.string.admin_customers_count);
            tvCustomersCount = cardCustomersCount.findViewById(R.id.tvKpiValue);
        }
        if (cardCustomersFiltered != null) {
            ((TextView) cardCustomersFiltered.findViewById(R.id.tvKpiLabel)).setText(R.string.admin_customers_search_results);
            tvCustomersFiltered = cardCustomersFiltered.findViewById(R.id.tvKpiValue);
        }
        tvCustomerResultCount = findViewById(R.id.tvCustomerResultCount);

        RecyclerView rv = findViewById(R.id.rvCustomers);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminCustomersAdapter(new AdminCustomersAdapter.Listener() {
                @Override
                public void onClick(NguoiDung user) {
                    showCustomerInfoDialog(user);
                }

                @Override
                public void onEdit(NguoiDung user) {
                    showCustomerDialog(user);
                }

                @Override
                public void onDelete(NguoiDung user) {
                    confirmDelete(user);
                }
            });
            rv.setAdapter(adapter);
        }

        MaterialButton btnAdd = findViewById(R.id.btnAddCustomer);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> showCustomerDialog(null));

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = AdminCustomersActivity.this::loadData;
                    searchHandler.postDelayed(searchRunnable, 250);
                }
            });
        }

        loadData();
    }

    private void loadData() {
        if (nguoiDungDao == null || adapter == null) return;
        String key = edtSearch != null ? edtSearch.getText().toString().trim() : "";
        int totalCustomers = nguoiDungDao.getCustomerCount();
        ArrayList<NguoiDung> filteredList = nguoiDungDao.layKhachHang(key);
        adapter.setData(filteredList);
        if (tvCustomersCount != null) tvCustomersCount.setText(String.valueOf(totalCustomers));
        if (tvCustomersFiltered != null) tvCustomersFiltered.setText(String.valueOf(filteredList.size()));
        if (tvCustomerResultCount != null) tvCustomerResultCount.setText(getString(R.string.admin_customer_results, filteredList.size()));
    }

    private void showCustomerInfoDialog(NguoiDung user) {
        StringBuilder info = new StringBuilder();
        info.append("Họ tên: ").append(valueOrDash(user.fullname)).append("\n");
        info.append("Tên tài khoản: @").append(user.username).append("\n");
        info.append("Vai trò: ").append(DBHelper.ROLE_CUSTOMER.equals(user.role) ? "Khách hàng" : user.role).append("\n");
        info.append("Trạng thái: ").append(user.isActive ? "Đang hoạt động" : "Bị khóa").append("\n\n");
        
        info.append("Thống kê mua hàng:\n");
        info.append("- Số đơn hàng: ").append(user.orderCount).append("\n");
        info.append("- Tổng chi tiêu: ").append(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(user.deliveredSpend)).append("đ");

        new AlertDialog.Builder(this)
                .setTitle("Thông tin cá nhân")
                .setMessage(info.toString())
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Sửa", (dialog, which) -> showCustomerDialog(user))
                .show();
    }

    private String valueOrDash(String value) {
        return (value == null || value.trim().isEmpty()) ? "---" : value.trim();
    }

    private void showCustomerDialog(NguoiDung oldUser) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_customer_form, null, false);

        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtConfirm = view.findViewById(R.id.edtConfirm);

        boolean editing = oldUser != null;
        if (editing) {
            if (edtFullName != null) edtFullName.setText(oldUser.fullname);
            if (edtUsername != null) {
                edtUsername.setText(oldUser.username);
                edtUsername.setEnabled(false);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? R.string.edit_customer : R.string.add_customer)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String fullname = edtFullName != null ? edtFullName.getText().toString().trim() : "";
            String username = edtUsername != null ? edtUsername.getText().toString().trim() : "";
            String pass = edtPassword != null ? edtPassword.getText().toString().trim() : "";
            String confirm = edtConfirm != null ? edtConfirm.getText().toString().trim() : "";

            if (TextUtils.isEmpty(fullname)) {
                Toast.makeText(this, R.string.err_fullname_required, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!editing) {
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(this, R.string.err_username_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                String passError = validatePassword(pass, confirm, true);
                if (passError != null) {
                    Toast.makeText(this, passError, Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ok = nguoiDungDao.dangKyKhachHang(fullname, username, pass);
                if (!ok) {
                    Toast.makeText(this, R.string.err_add_customer_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, R.string.customer_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData();
                return;
            }

            String passError = validatePassword(pass, confirm, false);
            if (passError != null) {
                Toast.makeText(this, passError, Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = nguoiDungDao.adminUpdateCustomer(oldUser.id, fullname, pass.isEmpty() ? null : pass);
            if (!ok) {
                Toast.makeText(this, R.string.customer_update_failed, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.customer_updated, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadData();
        }));

        dialog.show();
    }

    private String validatePassword(String pass, String confirm, boolean required) {
        if (required && TextUtils.isEmpty(pass)) return getString(R.string.err_password_required);
        if (required && TextUtils.isEmpty(confirm)) return getString(R.string.err_confirm_password_required);
        if (!required && pass.isEmpty() && confirm.isEmpty()) return null;
        if (pass.length() < 6) return "Mật khẩu phải có ít nhất 6 ký tự";
        if (!pass.equals(confirm)) return getString(R.string.err_password_not_match);
        return null;
    }

    private void confirmDelete(NguoiDung user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_customer_deactivate_title)
                .setMessage(getString(R.string.admin_customer_deactivate_confirm, user.username))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.admin_customer_deactivate_action, (d, w) -> {
                    boolean ok = nguoiDungDao.adminDeleteCustomer(user.id);
                    if (!ok) {
                        Toast.makeText(this, R.string.err_delete_customer_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, R.string.customer_deleted, Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }

}
