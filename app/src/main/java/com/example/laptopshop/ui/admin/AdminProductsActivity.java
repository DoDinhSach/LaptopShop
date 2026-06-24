package com.example.laptopshop.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.laptopshop.utils.ProductImageLoader;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.SanPhamDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;
import com.example.laptopshop.utils.InventoryPolicy;
import com.example.laptopshop.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

/**
 * Màn hình Quản lý Sản phẩm dành cho Admin
 * Kế thừa từ BaseHomeActivity để sử dụng khung Bottom Bar chung
 */
public class AdminProductsActivity extends BaseHomeActivity {

    // --- CÁC BIẾN QUẢN LÝ DỮ LIỆU ---
    private SessionManager session;        // Quản lý phiên đăng nhập (lưu user, role)
    private SanPhamDao sanPhamDao;        // Đối tượng truy vấn database bảng Sản phẩm
    private AdminProductAdapter adapter;  // Bộ đổ dữ liệu hiển thị danh sách sản phẩm

    // --- CÁC THÀNH PHẦN GIAO DIỆN (UI) ---
    private EditText edtSearch;           // Ô nhập từ khóa tìm kiếm sản phẩm
    private RecyclerView rvProducts;      // Danh sách cuộn hiển thị các laptop
    private View layoutEmpty;             // Giao diện hiển thị khi không có sản phẩm nào
    private TextView tvTotalProducts;     // Hiển thị tổng số lượng sản phẩm
    private TextView tvInStockProducts;   // Hiển thị số lượng sản phẩm còn hàng
    private TextView tvLowStockProducts;  // Hiển thị số lượng sản phẩm sắp hết
    private TextView tvStoppedProducts;   // Hiển thị số lượng sản phẩm đã ngưng bán
    private TextView tvResultCount;       // Hiển thị số lượng kết quả sau khi tìm kiếm

    // --- CÁC BIẾN HỖ TRỢ CHỌN ẢNH VÀ DIALOG ---
    private ActivityResultLauncher<String[]> imagePickerLauncher; // Trình khởi chạy chọn ảnh từ máy điện thoại
    private AlertDialog activeProductDialog;                      // Lưu trữ Dialog đang mở để xử lý chọn ảnh
    private EditText activeImageField;                            // Lưu trường nhập đường dẫn ảnh đang được chọn

    // Quy định layout của khung chứa (Header xanh)
    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    // Quy định layout nội dung bên trong (Danh sách sản phẩm và thống kê)
    @Override
    protected int contentLayoutRes() {
        return R.layout.activity_admin_products;
    }

    // Quy định menu dưới cùng cho Admin
    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_admin;
    }

    // Đánh dấu mục "Sản phẩm" đang được chọn trên Bottom Bar
    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_products;
    }

    @Override
    protected String screenTitle() {
        return getString(R.string.admin_products_title);
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

        // Khởi tạo trình chọn ảnh từ bộ nhớ máy
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null || activeImageField == null || activeProductDialog == null || !activeProductDialog.isShowing()) {
                return;
            }
            persistImageReadPermission(uri); // Cấp quyền đọc ảnh lâu dài cho App
            activeImageField.setText(uri.toString()); // Lưu đường dẫn ảnh vào ô nhập
        });

        // Kiểm tra bảo mật: Nếu không phải Admin thì buộc đăng xuất
        session = new SessionManager(this);
        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        sanPhamDao = new SanPhamDao(this);

        // Ánh xạ các View từ XML
        edtSearch = findViewById(R.id.edtSearch);
        rvProducts = findViewById(R.id.rvProducts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvInStockProducts = findViewById(R.id.tvInStockProducts);
        tvLowStockProducts = findViewById(R.id.tvLowStockProducts);
        tvStoppedProducts = findViewById(R.id.tvStoppedProducts);
        tvResultCount = findViewById(R.id.tvResultCount);

        // Thiết lập danh sách hiển thị dạng cột đứng
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter và các sự kiện khi bấm vào từng sản phẩm
        adapter = new AdminProductAdapter(new AdminProductAdapter.Listener() {
            @Override
            public void onEdit(SanPham product) {
                showProductFormDialog(product); // Mở form sửa sản phẩm
            }

            @Override
            public void onToggleSale(SanPham product) {
                confirmToggleSale(product); // Mở xác nhận ngưng/tiếp tục bán
            }

            @Override
            public void onDelete(SanPham product) {
                confirmDelete(product); // Mở xác nhận xóa sản phẩm
            }
        });
        rvProducts.setAdapter(adapter);

        // Nút "Thêm sản phẩm mới"
        MaterialButton btnAdd = findViewById(R.id.btnAddProduct);
        btnAdd.setOnClickListener(v -> showProductFormDialog(null));

        // Tự động tìm kiếm ngay khi người dùng đang nhập văn bản
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loadData(); // Tải lại danh sách theo từ khóa mới
            }
        });

        loadData(); // Tải dữ liệu lần đầu khi vào màn hình
    }

    /**
     * Tải dữ liệu từ database và cập nhật lên giao diện
     */
    private void loadData() {
        ArrayList<SanPham> allProducts = sanPhamDao.layTatCaChoAdmin(); // Lấy tất cả để thống kê số lượng
        String key = edtSearch.getText().toString().trim();
        ArrayList<SanPham> filteredProducts = key.isEmpty() ? allProducts : sanPhamDao.timKiemChoAdmin(key); // Lấy danh sách lọc

        adapter.setData(filteredProducts); // Đổ dữ liệu vào danh sách hiển thị
        updateDashboard(allProducts, filteredProducts); // Cập nhật các con số thống kê ở trên đầu
        updateEmptyState(filteredProducts); // Hiện thông báo nếu không tìm thấy gì
    }

    /**
     * Đồng bộ việc xem trước ảnh (Preview) khi thay đổi ô nhập đường dẫn ảnh
     */
    private void bindImagePreview(ImageView ivImagePreview, TextView tvImagePreviewHint, EditText edtImage, EditText edtName, Spinner spBrand) {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                refreshImagePreview(ivImagePreview, tvImagePreviewHint, edtImage, edtName, spBrand);
            }
        };
        edtImage.addTextChangedListener(watcher);
        edtName.addTextChangedListener(watcher);
        refreshImagePreview(ivImagePreview, tvImagePreviewHint, edtImage, edtName, spBrand);
    }

    /**
     * Làm mới hình ảnh hiển thị dựa trên URL/Uri hoặc Tên sản phẩm
     */
    private void refreshImagePreview(ImageView ivImagePreview, TextView tvImagePreviewHint, EditText edtImage, EditText edtName, Spinner spBrand) {
        String imageRef = edtImage.getText().toString().trim();
        String productName = edtName.getText().toString().trim();
        String brand = (String) spBrand.getSelectedItem();
        if ("Thương hiệu".equals(brand)) brand = "";

        ProductImageLoader.load(ivImagePreview, imageRef, productName, brand); // Gọi bộ nạp ảnh thông minh

        if (imageRef.isEmpty()) {
            tvImagePreviewHint.setText("Chưa chọn ảnh");
            return;
        }
        if (ProductImageLoader.isInvalidImageInput(imageRef)) {
            tvImagePreviewHint.setText("Ảnh không hợp lệ");
            return;
        }
        tvImagePreviewHint.setText("Đã chọn ảnh");
    }

    /**
     * Cập nhật 4 ô thống kê: Tổng, Còn hàng, Sắp hết, Ngưng bán
     */
    private void updateDashboard(ArrayList<SanPham> allProducts, ArrayList<SanPham> filteredProducts) {
        int total = allProducts == null ? 0 : allProducts.size();
        int inStock = 0;
        int lowStock = 0;
        int stopped = 0;

        if (allProducts != null) {
            for (SanPham p : allProducts) {
                if (p == null) continue;
                if (!p.isActive) {
                    stopped++; // Đã bị Admin xóa tạm thời hoặc ngưng kinh doanh
                    continue;
                }
                if (!InventoryPolicy.isOutOfStock(p.tonKho)) inStock++; // Còn hàng (>0)
                if (InventoryPolicy.isLowStock(p)) lowStock++; // Sắp hết hàng (theo chính sách kho)
            }
        }

        tvTotalProducts.setText(String.valueOf(total));
        tvInStockProducts.setText(String.valueOf(inStock));
        tvLowStockProducts.setText(String.valueOf(lowStock));
        tvStoppedProducts.setText(String.valueOf(stopped));

        int resultCount = filteredProducts == null ? 0 : filteredProducts.size();
        tvResultCount.setText(getString(R.string.admin_product_results, resultCount));
    }

    /**
     * Ẩn/hiện danh sách dựa trên việc có dữ liệu hay không
     */
    private void updateEmptyState(ArrayList<SanPham> list) {
        boolean isEmpty = list == null || list.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * Lưu quyền đọc file ảnh lâu dài (để sau khi khởi động lại App vẫn xem được ảnh đã chọn)
     */
    private void persistImageReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (RuntimeException ignored) {
            Toast.makeText(this, R.string.product_image_permission_not_persisted, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Hiển thị Hộp thoại (Dialog) để Thêm hoặc Sửa thông tin Laptop
     */
    private void showProductFormDialog(SanPham oldProduct) {
        // Nạp giao diện form từ XML
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_product_form, null, false);

        // Ánh xạ các trường nhập liệu trong Form
        EditText edtName = view.findViewById(R.id.edtName);
        Spinner spBrand = view.findViewById(R.id.spBrand);
        EditText edtImage = view.findViewById(R.id.edtImage);
        MaterialButton btnChooseImage = view.findViewById(R.id.btnChooseImage);
        ImageView ivImagePreview = view.findViewById(R.id.ivImagePreview);
        TextView tvImagePreviewHint = view.findViewById(R.id.tvImagePreviewHint);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtDiscount = view.findViewById(R.id.edtDiscount);
        EditText edtDesc = view.findViewById(R.id.edtDesc);
        
        // Các Spinner chọn cấu hình máy
        Spinner spType = view.findViewById(R.id.spType);
        Spinner spScreenSize = view.findViewById(R.id.spScreenSize);
        Spinner spCpu = view.findViewById(R.id.spCpu);
        Spinner spRam = view.findViewById(R.id.spRam);
        Spinner spSsd = view.findViewById(R.id.spSsd);
        Spinner spRefreshRate = view.findViewById(R.id.spRefreshRate);
        Spinner spResolution = view.findViewById(R.id.spResolution);

        // Đổ dữ liệu vào các Spinner (Hãng, CPU, RAM...)
        setupDialogSpinners(spBrand, spType, spScreenSize, spCpu, spRam, spSsd, spRefreshRate, spResolution);

        boolean editing = oldProduct != null;
        if (editing) {
            // Nếu là Sửa, nạp dữ liệu cũ vào các ô nhập
            edtName.setText(oldProduct.tenSanPham);
            edtImage.setText(oldProduct.tenAnh);
            edtPrice.setText(String.valueOf(oldProduct.gia));
            edtDiscount.setText(String.valueOf(oldProduct.giamGia));
            edtDesc.setText(oldProduct.moTa);

            // Chọn đúng mục trong Spinner theo dữ liệu cũ
            setSpinnerSelection(spBrand, oldProduct.hang);
            setSpinnerSelection(spType, oldProduct.loaiSanPham);
            setSpinnerSelection(spScreenSize, oldProduct.manHinh);
            setSpinnerSelection(spCpu, oldProduct.chipset);
            setSpinnerSelection(spRam, oldProduct.ramGb + "GB");
            setSpinnerSelection(spSsd, oldProduct.romGb + "GB");
            setSpinnerSelection(spRefreshRate, oldProduct.tanSoQuet + "Hz");
            setSpinnerSelection(spResolution, oldProduct.doPhanGiai);
        }

        bindImagePreview(ivImagePreview, tvImagePreviewHint, edtImage, edtName, spBrand);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Sửa sản phẩm" : "Thêm sản phẩm mới")
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        // Xử lý nút "Chọn ảnh từ máy"
        btnChooseImage.setOnClickListener(v -> {
            activeProductDialog = dialog;
            activeImageField = edtImage;
            imagePickerLauncher.launch(new String[]{"image/*"}); // Mở thư viện ảnh
        });

        dialog.setOnShowListener(d -> {
            activeProductDialog = dialog;
            // Xử lý khi bấm nút "Lưu"
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                SanPham p = editing ? oldProduct : new SanPham();
                // Thu thập và kiểm tra tính hợp lệ dữ liệu
                String error = fillAndValidateProductFromForm(
                        p, editing, edtName, spBrand, edtImage, edtPrice, edtDiscount,
                        spType, spScreenSize, spCpu, spRam, spSsd, spRefreshRate, spResolution, edtDesc
                );

                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); // Hiện thông báo lỗi nếu nhập thiếu/sai
                    return;
                }

                boolean ok;
                if (editing) {
                    ok = sanPhamDao.update(p); // Cập nhật vào DB
                } else {
                    ok = sanPhamDao.insert(p) != -1; // Thêm mới vào DB
                }

                if (!ok) {
                    Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, editing ? "Cập nhật thành công" : "Thêm thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadData(); // Tải lại danh sách sau khi lưu
            });
        });
        
        dialog.setOnDismissListener(d -> {
            if (activeProductDialog == dialog) {
                activeProductDialog = null;
                activeImageField = null;
            }
        });

        dialog.show();
    }

    /**
     * Đọc dữ liệu từ các ô nhập liệu và kiểm tra tính hợp lệ
     * Trả về thông báo lỗi nếu không hợp lệ, trả về null nếu OK
     */
    private String fillAndValidateProductFromForm(SanPham p, boolean editing, EditText edtName, Spinner spBrand, EditText edtImage, EditText edtPrice, EditText edtDiscount, Spinner spType, Spinner spScreenSize, Spinner spCpu, Spinner spRam, Spinner spSsd, Spinner spRefreshRate, Spinner spResolution, EditText edtDesc) {

        String name = edtName.getText().toString().trim();
        String brand = (String) spBrand.getSelectedItem();
        String image = edtImage.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();

        String type = (String) spType.getSelectedItem();
        String screen = (String) spScreenSize.getSelectedItem();
        String cpu = (String) spCpu.getSelectedItem();
        String ramStr = (String) spRam.getSelectedItem();
        String ssdStr = (String) spSsd.getSelectedItem();
        String rateStr = (String) spRefreshRate.getSelectedItem();
        String resolution = (String) spResolution.getSelectedItem();

        // Ràng buộc các trường không được để trống
        if (TextUtils.isEmpty(name)) return "Vui lòng nhập tên sản phẩm";
        if ("Thương hiệu".equals(brand)) return "Vui lòng chọn thương hiệu";
        if (TextUtils.isEmpty(priceStr)) return "Vui lòng nhập giá bán";
        if ("Loại sản phẩm".equals(type)) return "Vui lòng chọn loại sản phẩm";
        if ("Màn hình".equals(screen)) return "Vui lòng chọn kích thước màn hình";
        if ("CPU".equals(cpu)) return "Vui lòng chọn CPU";
        if ("RAM".equals(ramStr)) return "Vui lòng chọn dung lượng RAM";
        if ("SSD".equals(ssdStr)) return "Vui lòng chọn dung lượng SSD";
        if ("Tần số quét".equals(rateStr)) return "Vui lòng chọn tần số quét";
        if ("Độ phân giải".equals(resolution)) return "Vui lòng chọn độ phân giải";

        int price;
        int discount;
        try {
            price = Integer.parseInt(priceStr);
            discount = TextUtils.isEmpty(discountStr) ? 0 : Integer.parseInt(discountStr);
        } catch (NumberFormatException e) {
            return "Định dạng số không hợp lệ";
        }

        if (price <= 0) return "Giá bán phải lớn hơn 0";
        if (discount < 0 || discount >= 100) return "Giảm giá không hợp lệ (0-99%)";

        // Gán dữ liệu vào đối tượng Sản phẩm
        p.tenSanPham = name;
        p.hang = brand;
        p.tenAnh = image;
        p.gia = price;
        p.giamGia = discount;
        p.loaiSanPham = type;
        p.manHinh = screen;
        p.chipset = cpu;
        p.ramGb = Integer.parseInt(ramStr.replace("GB", ""));
        p.romGb = Integer.parseInt(ssdStr.replace("GB", ""));
        if (p.romGb == 1 || p.romGb == 2) p.romGb *= 1024; // Chuyển đổi TB sang GB
        p.tanSoQuet = Integer.parseInt(rateStr.replace("Hz", ""));
        p.doPhanGiai = resolution;
        p.moTa = edtDesc.getText().toString().trim();

        if (!editing) {
            p.tonKho = 0; // Sản phẩm mới mặc định tồn kho bằng 0
            p.isActive = true;
        }

        return null;
    }

    /**
     * Khởi tạo dữ liệu cho tất cả các Spinner trong form
     */
    private void setupDialogSpinners(Spinner spBrand, Spinner spType, Spinner spScreenSize, Spinner spCpu, Spinner spRam, Spinner spSsd, Spinner spRefreshRate, Spinner spResolution) {
        bindDialogSpinner(spBrand, new String[]{"Thương hiệu", "Apple", "Dell", "Asus", "Lenovo", "HP", "MSI"});
        bindDialogSpinner(spType, new String[]{"Loại sản phẩm", "Laptop Văn Phòng", "Laptop Gaming", "Laptop Doanh Nhân", "Laptop Mỏng Nhẹ", "MacBook Air"});
        bindDialogSpinner(spScreenSize, new String[]{"Màn hình", "Dưới 14 inch", "14 inch", "15.6 inch", "16 inch trở lên"});
        bindDialogSpinner(spCpu, new String[]{"CPU", "Intel Core i3", "Intel Core i5", "Intel Core i7", "Intel Core i9", "Intel Core Ultra 5", "Intel Core Ultra 7", "AMD Ryzen 7", "AMD Ryzen 9", "Apple M1", "Apple M2", "Apple M3", "Apple M4"});
        bindDialogSpinner(spRam, new String[]{"RAM", "8GB", "16GB", "32GB", "64GB"});
        bindDialogSpinner(spSsd, new String[]{"SSD", "256GB", "512GB", "1TB", "2TB"});
        bindDialogSpinner(spRefreshRate, new String[]{"Tần số quét", "60Hz", "90Hz", "120Hz", "165Hz"});
        bindDialogSpinner(spResolution, new String[]{"Độ phân giải", "HD", "Full HD", "2K", "2.8K", "3K", "4K"});
    }

    private void bindDialogSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected_admin, items);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_admin);
        spinner.setAdapter(adapter);
    }

    /**
     * Tìm và chọn mục đúng trong Spinner dựa trên giá trị text
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Xác nhận Ngưng bán hoặc Tiếp tục kinh doanh sản phẩm
     */
    private void confirmToggleSale(SanPham product) {
        boolean stopSelling = product.isActive;
        new AlertDialog.Builder(this)
                .setTitle(stopSelling ? R.string.admin_stop_selling_product_title : R.string.admin_resume_selling_product_title)
                .setMessage(getString(
                        stopSelling ? R.string.admin_stop_selling_product_message : R.string.admin_resume_selling_product_message,
                        product.tenSanPham
                ))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(stopSelling ? R.string.admin_stop_selling_product_action : R.string.admin_resume_selling_product_action, (d, w) -> {
                    boolean ok = sanPhamDao.setActive(product.maSanPham, !stopSelling);
                    if (ok) {
                        Toast.makeText(this, stopSelling ? R.string.product_stopped_selling : R.string.product_resumed_selling, Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Xác nhận xóa sản phẩm khỏi hệ thống
     */
    private void confirmDelete(SanPham product) {
        // Kiểm tra điều kiện xóa (Tồn kho phải bằng 0, không có đơn hàng đang xử lý...)
        SanPhamDao.SanPhamDeactivationCheck check = sanPhamDao.checkDeactivationEligibility(product.maSanPham);
        if (!check.canDeactivate()) {
            Toast.makeText(this, buildDeactivateBlockedMessage(check), Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_deactivate_product_title)
                .setMessage(getString(R.string.admin_deactivate_product_message, product.tenSanPham))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.admin_deactivate_product_action, (d, w) -> {
                    boolean ok = sanPhamDao.delete(product.maSanPham);
                    if (ok) {
                        Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, buildDeactivateBlockedMessage(sanPhamDao.checkDeactivationEligibility(product.maSanPham)), Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    /**
     * Xây dựng thông báo khi không đủ điều kiện xóa sản phẩm
     */
    private String buildDeactivateBlockedMessage(SanPhamDao.SanPhamDeactivationCheck check) {
        if (check == null || check.sanPham == null) {
            return getString(R.string.action_failed);
        }
        if (check.currentStock > 0) {
            return getString(R.string.admin_product_deactivate_blocked_stock, check.currentStock);
        }
        if (check.activeCartCount > 0) {
            return getString(R.string.admin_product_deactivate_blocked_cart, check.activeCartCount);
        }
        if (check.openOrderCount > 0) {
            return getString(R.string.admin_product_deactivate_blocked_order, check.openOrderCount);
        }
        return getString(R.string.action_failed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sanPhamDao != null && adapter != null) {
            loadData(); // Tải lại dữ liệu khi quay lại màn hình để đảm bảo thông tin luôn mới nhất
        }
    }
}
