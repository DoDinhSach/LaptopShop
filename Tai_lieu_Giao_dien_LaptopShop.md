# TÀI LIỆU CHI TIẾT CẤU TRÚC GIAO DIỆN (UI) - DỰ ÁN LAPTOPSHOP

Tài liệu này liệt kê tất cả các thành phần giao diện, chức năng và mối liên kết giữa mã nguồn Java và tệp Layout XML.

---

## 1. HỆ THỐNG KHUNG CHUNG (CORE SHELL)
Hệ thống này giúp ứng dụng có thanh menu cố định và chỉ thay đổi nội dung ở giữa.

| Tên File Java | Tên File Layout XML | Tác dụng | Liên kết / Thành phần con |
| :--- | :--- | :--- | :--- |
| **BaseHomeActivity.java** | `activity_home_bottom.xml` | Lớp cha quản lý điều hướng toàn cục. | Tự động nạp nội dung vào `homeContentContainer`. |
| (Không có) | `activity_home_bottom_admin.xml` | Khung giao diện riêng cho Admin (Header xanh). | Chứa `view_admin_bottom_bar.xml`. |
| (Không có) | `activity_home_bottom_customer.xml` | Khung giao diện cho Khách hàng. | Chứa BottomNavigationView mặc định. |
| (Không có) | `view_admin_bottom_bar.xml` | Thanh điều hướng 6 nút của Admin. | Ánh xạ ID đến các Activity quản trị. |

---

## 2. PHÂN HỆ QUẢN TRỊ (ADMIN MODULE)
Dành cho người quản lý điều hành cửa hàng.

| Tên Activity (Java) | Tên Layout XML | Tác dụng | Liên kết Adapter / Item |
| :--- | :--- | :--- | :--- |
| **AdminHomeActivity** | `content_admin_dashboard.xml` | Trang thống kê chính (Dashboard). | `item_admin_dashboard_entry.xml` |
| **AdminProductsActivity** | `activity_admin_products.xml` | Quản lý danh mục sản phẩm. | `AdminProductAdapter` -> `item_admin_product.xml` |
| **AdminOrdersActivity** | `activity_admin_orders.xml` | Danh sách đơn hàng toàn hệ thống. | `OrdersAdapter` -> `item_order_customer.xml` |
| **AdminCustomersActivity** | `content_admin_customers.xml` | Quản lý tài khoản khách hàng. | `AdminCustomersAdapter` -> `item_admin_customer.xml` |
| **AdminReportsActivity** | `content_admin_reports.xml` | Báo cáo doanh thu bằng biểu đồ. | Sử dụng MPAndroidChart (Line/Bar/Pie). |
| **AdminInventoryActivity** | `content_admin_inventory_nav.xml`| Menu điều hướng kho. | Các nút mở Overview, Receipts, Suppliers. |
| **AdminReceiptsActivity** | `content_admin_receipts.xml` | Lịch sử nhập hàng từ nhà cung cấp. | `ReceiptAdapter` -> `item_receipt.xml` |
| **AdminVouchersActivity** | `content_admin_vouchers.xml` | Quản lý mã giảm giá. | `GiamGiaAdapter` -> `item_admin_voucher.xml` |

---

## 3. PHÂN HỆ KHÁCH HÀNG (CUSTOMER MODULE)
Dành cho người dùng mua sắm.

| Tên Activity (Java) | Tên Layout XML | Tác dụng | Liên kết Adapter / Item |
| :--- | :--- | :--- | :--- |
| **CustomerHomeActivity** | `content_home.xml` | Trang chủ (Banner, hãng, gợi ý). | `FeaturedHomeAdapter` -> `item_featured_home.xml` |
| **ProductsActivity** | `activity_products.xml` | Danh sách sản phẩm & Bộ lọc. | `ProductAdapter` -> `item_product.xml` |
| **ProductDetailActivity** | `activity_product_detail.xml` | Xem chi tiết, thông số kỹ thuật. | Hiển thị thông tin từ `SanPham` model. |
| **CartActivity** | `activity_cart.xml` | Giỏ hàng cá nhân. | `CartAdapter` -> `item_cart.xml` |
| **CheckoutActivity** | `activity_checkout.xml` | Thanh toán và chọn Voucher. | `CustomerVoucherAdapter` |
| **OrdersActivity** | `activity_orders.xml` | Theo dõi đơn hàng đã mua. | `OrdersAdapter` -> `item_order_customer.xml` |
| **ProfileActivity** | `activity_profile.xml` | Thông tin cá nhân & Đổi mật khẩu. | Liên kết `NguoiDungDao`. |

---

## 4. DANH SÁCH & HỘP THOẠI (ADAPTERS & DIALOGS)
Các thành phần hiển thị danh sách và cửa sổ nhập liệu.

| Tên File XML | Loại | Tác dụng |
| :--- | :--- | :--- |
| **item_product.xml** | Item | Ô hiển thị sản phẩm dạng lưới (Grid) cho khách. |
| **item_admin_product.xml** | Item | Dòng hiển thị sản phẩm kèm nút Sửa/Xóa cho Admin. |
| **item_order_customer.xml** | Item | Thẻ hiển thị đơn hàng kèm trạng thái (Chờ duyệt/Đã giao). |
| **dialog_admin_product_form.xml**| Dialog | Biểu mẫu nhập thông số Laptop (CPU, RAM, SSD...). |
| **dialog_admin_customer_form.xml**| Dialog | Biểu mẫu tạo mới/cập nhật khách hàng. |
| **dialog_voucher_form.xml** | Dialog | Biểu mẫu thiết lập mã giảm giá. |
| **dialog_supplier_form.xml** | Dialog | Biểu mẫu quản lý thông tin nhà cung cấp. |

---

## 5. MÀN HÌNH CHỨC NĂNG PHỤ
| Tên Activity (Java) | Tên Layout XML | Tác dụng |
| :--- | :--- | :--- |
| **LoginActivity** | `activity_login.xml` | Đăng nhập hệ thống. |
| **RegisterActivity** | `activity_register.xml` | Đăng ký tài khoản mới. |
| **WelcomeActivity** | `activity_welcome.xml` | Màn hình giới thiệu khi mở App. |
| **RoutingActivity** | (Không có) | Tự động điều hướng dựa trên Session (Admin/User). |

---
*Tài liệu được cập nhật tự động theo cấu trúc dự án LaptopShop.*
