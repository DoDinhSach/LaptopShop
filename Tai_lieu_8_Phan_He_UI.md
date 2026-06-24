# TÀI LIỆU PHÂN TÍCH CHI TIẾT 8 PHÂN HỆ UI (USER INTERFACE)

Tài liệu này cung cấp cái nhìn chi tiết nhất về từng file code trong 8 thư mục thuộc phân hệ UI của dự án LaptopShop.

---

## 1. PHÂN HỆ `ui/admin` (Quản lý hệ thống)
Tập trung vào các chức năng dành cho người quản trị để điều hành cửa hàng.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Luồng xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **AdminProductsActivity** | Màn hình chính quản lý toàn bộ sản phẩm laptop. | `activity_admin_products.xml`, `dialog_admin_product_form.xml` | Truy vấn `SanPhamDao`, xử lý thêm mới, chỉnh sửa thông số kỹ thuật (CPU, RAM...) và trạng thái bán. |
| **AdminProductAdapter** | Chuyển đổi dữ liệu Sản phẩm thành giao diện danh sách. | `item_admin_product.xml` | Gắn dữ liệu vào từng ô (Card), xử lý sự kiện khi Admin bấm nút Sửa, Ngưng bán hoặc Xóa. |
| **AdminCustomersActivity** | Quản lý danh sách khách hàng và quyền truy cập. | `content_admin_customers.xml`, `dialog_admin_customer_form.xml` | Lấy danh sách từ `NguoiDungDao`, hiển thị thống kê chi tiêu và trạng thái tài khoản (Hoạt động/Khóa). |
| **AdminCustomersAdapter** | Hiển thị thông tin khách hàng trong danh sách. | `item_admin_customer.xml` | Hiển thị tên, ảnh đại diện, tổng tiền đã mua và các nút tương tác với tài khoản khách. |
| **AdminReportsActivity** | Phân tích và báo cáo số liệu kinh doanh. | `content_admin_reports.xml` | Sử dụng `MPAndroidChart` để vẽ biểu đồ doanh thu theo thời gian và tỷ lệ sản phẩm bán chạy. |

---

## 2. PHÂN HỆ `ui/auth` (Xác thực người dùng)
Quản lý bảo mật, quyền truy cập và luồng bắt đầu của ứng dụng.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Luồng xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **RoutingActivity** | Kiểm soát điều hướng thông minh khi mở App. | (Không có layout) | Đọc `SessionManager`. Phân loại user: Admin -> Dashboard; Customer -> Home; Chưa login -> Welcome. |
| **WelcomeActivity** | Màn hình giới thiệu và tiếp thị ban đầu. | `activity_welcome.xml` | Hiển thị các tính năng nổi bật, cung cấp nút dẫn tới Đăng nhập và Đăng ký. |
| **LoginActivity** | Xử lý đăng nhập tài khoản. | `activity_login.xml` | Nhận input, kiểm tra với `NguoiDungDao`, lưu Session và Token sau khi xác thực thành công. |
| **RegisterActivity** | Xử lý tạo tài khoản khách hàng mới. | `activity_register.xml` | Kiểm tra tính hợp lệ của thông tin, kiểm tra trùng lặp và lưu người dùng mới vào DB. |

---

## 3. PHÂN HỆ `ui/cart` (Giỏ hàng)
Quản lý các vật phẩm người dùng dự định mua.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Luồng xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **CartActivity** | Màn hình quản lý vật phẩm trong giỏ. | `activity_cart.xml` | Tính toán tổng tiền, kiểm tra tồn kho thời gian thực, quản lý các thay đổi số lượng của user. |
| **CartAdapter** | Hiển thị từng món hàng trong giỏ. | `item_cart.xml` | Xử lý tăng/giảm số lượng trực tiếp trên từng Item và cập nhật lại giao diện tổng tiền. |

---

## 4. PHÂN HỆ `ui/checkout` (Thanh toán)
Hoàn tất quá trình mua hàng và tạo hóa đơn.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Luồng xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **CheckoutActivity** | Màn hình xác nhận đơn hàng và thanh toán. | `activity_checkout.xml` | Tổng hợp thông tin nhận hàng, áp dụng Voucher, chọn phương thức thanh toán và tạo đơn hàng. |
| **CustomerVoucherAdapter** | Hiển thị danh sách mã giảm giá khả dụng. | `item_customer_voucher_selectable.xml` | Lọc các Voucher phù hợp với giá trị đơn hàng hiện tại để người dùng lựa chọn. |

---

## 5. PHÂN HỆ `ui/home` (Trang chủ & Nghiệp vụ nội bộ)
Đây là phân hệ lớn nhất, chứa khung sườn và các nghiệp vụ quản lý kho.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Workflow chính |
| :--- | :--- | :--- | :--- |
| **BaseHomeActivity** | Lớp cha quy định giao diện khung của App. | `activity_home_bottom.xml` | Cung cấp thanh Bottom Navigation và quản lý tiêu đề màn hình dùng chung. |
| **AdminHomeActivity** | Dashboard tổng hợp cho Quản trị viên. | `content_admin_dashboard.xml` | Hiển thị nhanh các chỉ số: Doanh thu tháng, Đơn hàng mới, Cảnh báo kho. |
| **CustomerHomeActivity** | Trang chủ chính cho người mua hàng. | `content_home.xml` | Hiển thị Banner quảng cáo, các danh mục hãng (Apple, Dell...) và sản phẩm hot. |
| **AdminInventoryActivity** | Trung tâm quản lý kho (Hub). | `content_admin_inventory_nav.xml` | Menu điều phối sang các chức năng: Nhập hàng, Lịch sử, Nhà cung cấp. |
| **AdminReceiptsActivity** | Quản lý phiếu nhập hàng. | `content_admin_receipts.xml` | Theo dõi danh sách các lần nhập hàng từ các đối tác cung cấp. |
| **AdminSuppliersActivity** | Quản lý thông tin các nhà cung cấp. | `activity_admin_suppliers.xml` | Thêm/Sửa/Xóa thông tin liên hệ và hãng phân phối của đối tác. |
| **AdminVouchersActivity** | Quản lý các mã giảm giá hệ thống. | `content_admin_vouchers.xml` | Thiết lập mã, mức giảm, đơn tối thiểu và thời hạn cho các chiến dịch. |
| **InventoryHistoryAdapter** | Hiển thị lịch sử biến động kho. | `item_inventory_history.xml` | Ghi lại mọi lần tăng/giảm số lượng sản phẩm (do nhập hàng hoặc bán hàng). |

---

## 6. PHÂN HỆ `ui/orders` (Quản lý đơn hàng)
Xử lý toàn bộ vòng đời của một đơn hàng.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Workflow chính |
| :--- | :--- | :--- | :--- |
| **OrdersActivity** | Danh sách lịch sử mua hàng của khách. | `activity_orders.xml` | Hiển thị các đơn hàng theo trạng thái: Chờ duyệt, Đang giao, Đã giao, Đã hủy. |
| **OrderDetailActivity** | Chi tiết của một hóa đơn cụ thể. | `activity_order_detail_customer.xml` | Hiển thị danh sách sản phẩm đã mua, phí vận chuyển, tổng tiền và địa chỉ nhận. |
| **OrdersAdapter** | Adapter hiển thị thẻ đơn hàng. | `item_order_customer.xml` | Định dạng hiển thị mã đơn, ngày đặt, tổng tiền và chip trạng thái màu sắc. |
| **OrderItemsAdapter** | Hiển thị danh sách sản phẩm bên trong đơn. | `item_order_item.xml` | Liệt kê chi tiết từng máy: Tên máy, giá bán lúc mua, số lượng và thành tiền. |

---

## 7. PHÂN HỆ `ui/products` (Sản phẩm & Tìm kiếm)
Phục vụ nhu cầu tìm kiếm và xem sản phẩm của khách hàng.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Workflow chính |
| :--- | :--- | :--- | :--- |
| **ProductsActivity** | Catalog sản phẩm toàn diện. | `activity_products.xml` | Xử lý bộ lọc đa năng: lọc theo hãng, giá, dung lượng RAM, SSD, độ phân giải màn hình. |
| **ProductDetailActivity** | Màn hình chi tiết cấu hình máy. | `activity_product_detail.xml` | Hiển thị toàn bộ thông số: Chipset, Màn hình, Pin... Xử lý thêm vào giỏ hàng. |
| **ProductAdapter** | Hiển thị sản phẩm dạng lưới (Grid). | `item_product.xml` | Trình bày sản phẩm đẹp mắt cho khách hàng, hiển thị giá gốc và giá sau giảm giá. |

---

## 8. PHÂN HỆ `ui/profile` (Cá nhân hóa)
Quản lý thông tin người dùng và cài đặt.

| Tên File | Tác dụng chi tiết | Layout & Liên kết | Workflow chính |
| :--- | :--- | :--- | :--- |
| **ProfileActivity** | Màn hình hồ sơ cá nhân người dùng. | `activity_profile.xml` | Cho phép cập nhật Họ tên, SĐT và quản lý chức năng Đổi mật khẩu hoặc Đăng xuất. |

---
**Tổng kết kỹ thuật:** 8 phân hệ này tạo thành một hệ sinh thái UI khép kín, trong đó dữ liệu được tách biệt rõ ràng giữa vai trò **Quản trị (Admin)** và **Khách hàng (Customer)** thông qua các thư mục chuyên biệt.
