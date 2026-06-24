# TÀI LIỆU CẤU TRÚC CHI TIẾT MÃ NGUỒN UI (USER INTERFACE)

Tài liệu này phân tích chuyên sâu các thành phần giao diện, luồng xử lý dữ liệu và mối quan hệ giữa các màn hình trong dự án LaptopShop.

---

## 1. PHÂN HỆ `ui/admin` - QUẢN TRỊ HỆ THỐNG
Dành cho người quản lý để kiểm soát toàn bộ hoạt động của cửa hàng.

| Tên File | Chức năng chi tiết | Layout & Thành phần liên quan | Quy trình xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **AdminProductsActivity** | Trung tâm quản lý kho sản phẩm. | `activity_admin_products.xml`, `dialog_admin_product_form.xml` | Lấy danh sách từ `SanPhamDao`. Cho phép lọc, tìm kiếm và mở Dialog để thêm/sửa thông số Laptop (CPU, RAM, SSD...). |
| **AdminProductAdapter** | Hiển thị sản phẩm dạng danh sách rút gọn cho Admin. | `item_admin_product.xml` | Hiển thị ảnh, tên, giá, số lượng tồn kho. Có 3 nút chức năng trực tiếp: Sửa, Ngưng bán (Toggle), Xóa. |
| **AdminCustomersActivity**| Quản lý thông tin người dùng. | `content_admin_customers.xml`, `dialog_admin_customer_form.xml` | Hiển thị tổng số khách hàng. Cho phép xem chi tiết chi tiêu của từng khách và khóa/mở khóa tài khoản. |
| **AdminReportsActivity** | Phân tích dữ liệu kinh doanh. | `content_admin_reports.xml` | Sử dụng thư viện `MPAndroidChart` để vẽ biểu đồ doanh thu theo tháng, thống kê sản phẩm bán chạy và tỷ lệ đơn hàng. |

---

## 2. PHÂN HỆ `ui/auth` - XÁC THỰC & ĐIỀU HƯỚNG
Xử lý bảo mật và quyền truy cập vào ứng dụng.

| Tên File | Chức năng chi tiết | Layout liên kết | Quy trình xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **RoutingActivity** | "Bộ não" điều hướng ban đầu. | (Không có layout) | Kiểm tra `SessionManager`. Nếu đã đăng nhập và là Admin -> vào trang Quản trị; là Customer -> vào trang Chủ; chưa đăng nhập -> vào Welcome. |
| **WelcomeActivity** | Màn hình tiếp thị (Landing Page). | `activity_welcome.xml` | Giới thiệu các tính năng nổi bật và cung cấp lối tắt vào Đăng nhập/Đăng ký. |
| **LoginActivity** | Xác thực người dùng. | `activity_login.xml` | Kiểm tra username/password từ `NguoiDungDao`. Nếu đúng, lưu thông tin vào Session và chuyển hướng theo vai trò. |
| **RegisterActivity** | Tạo tài khoản khách hàng. | `activity_register.xml` | Thu thập thông tin, kiểm tra trùng lặp username và mã hóa mật khẩu cơ bản trước khi lưu vào SQLite. |

---

## 3. PHÂN HỆ `ui/home` - GIAO DIỆN CHÍNH & TIỆN ÍCH KHO
Quản lý các màn hình Dashboard và nghiệp vụ kho bãi phức tạp.

| Tên File | Chức năng chi tiết | Layout liên kết | Quy trình xử lý (Workflow) |
| :--- | :--- | :--- | :--- |
| **BaseHomeActivity** | Khung sườn chung cho toàn App. | `activity_home_bottom.xml` | Quản lý Bottom Navigation. Các Activity khác kế thừa file này để không phải viết lại code cho thanh menu dưới. |
| **AdminHomeActivity** | Dashboard tổng quát cho Admin. | `content_admin_dashboard.xml` | Hiển thị các chỉ số nhanh: Doanh thu hôm nay, Đơn hàng mới, Cảnh báo hết hàng trong kho. |
| **CustomerHomeActivity** | Trang chủ cho người mua. | `content_home.xml` | Hiển thị Slide Banner, danh sách các hãng (Apple, Dell...) và các sản phẩm đang giảm giá mạnh. |
| **AdminInventoryActivity**| Quản lý kho tổng quát. | `content_admin_inventory_nav.xml`| Cung cấp các nút dẫn đến: Quản lý nhà cung cấp, Phiếu nhập hàng và Lịch sử biến động kho. |
| **AdminReceiptsActivity** | Quản lý phiếu nhập hàng. | `content_admin_receipts.xml` | Theo dõi các lần nhập hàng từ nhà cung cấp để tăng số lượng tồn kho trong hệ thống. |
| **AdminVouchersActivity** | Quản lý khuyến mãi. | `content_admin_vouchers.xml` | Thiết lập mã giảm giá theo số tiền hoặc theo phần trăm cho các chiến dịch marketing. |

---

## 4. PHÂN HỆ `ui/products` & `ui/orders` - NGHIỆP VỤ MUA SẮM
Luồng xử lý từ lúc xem hàng đến khi hoàn tất đơn hàng.

| Thư mục | Tên File | Chức năng chi tiết | Workflow |
| :--- | :--- | :--- | :--- |
| **products** | **ProductsActivity** | Catalog sản phẩm toàn diện. | Chứa bộ lọc đa năng: lọc theo giá, theo RAM, CPU hoặc hãng sản xuất. |
| **products** | **ProductDetailActivity**| Chi tiết máy & Đặt hàng. | Hiển thị toàn bộ thông số kỹ thuật. Cho phép người dùng thêm sản phẩm vào giỏ hàng. |
| **orders** | **OrdersActivity** | Lịch sử đơn hàng của khách. | Hiển thị danh sách đơn hàng đã mua. Phân loại theo: Chờ xác nhận, Đang giao, Đã giao. |
| **orders** | **OrderDetailActivity** | Chi tiết hóa đơn. | Hiển thị từng sản phẩm trong đơn, địa chỉ giao hàng và tổng tiền thanh toán cuối cùng. |

---

## 5. PHÂN HỆ `ui/cart`, `ui/checkout` & `ui/profile` - CÁ NHÂN HÓA
Quản lý giỏ hàng, thanh toán và thông tin cá nhân.

| Thư mục | Tên File | Chức năng chi tiết | Workflow |
| :--- | :--- | :--- | :--- |
| **cart** | **CartActivity** | Quản lý vật phẩm đã chọn. | Thay đổi số lượng, xóa sản phẩm khỏi giỏ. Tính toán tổng tiền tạm tính thời gian thực. |
| **checkout** | **CheckoutActivity** | Hoàn tất mua hàng. | Xác nhận địa chỉ, chọn phương thức thanh toán và áp dụng mã giảm giá (Voucher). |
| **profile** | **ProfileActivity** | Hồ sơ người dùng. | Cập nhật họ tên, số điện thoại và quản lý việc đổi mật khẩu bảo mật. |

---

### TỔNG KẾT MỐI QUAN HỆ KỸ THUẬT:
1.  **Kiến trúc kế thừa**: Hầu hết Activity đều `extends BaseHomeActivity` để tạo ra trải nghiệm chuyển tab mượt mà.
2.  **Mô hình dữ liệu**: Activity gọi **DAO** (Data Access Object) -> DAO truy vấn **SQLite** -> Trả về **Model** (SanPham, DonHang) -> Activity chuyển Model vào **Adapter**.
3.  **Tương tác UI**: Các Dialog (Hộp thoại) được tách riêng layout để tái sử dụng trong cả việc Thêm mới và Chỉnh sửa dữ liệu, giúp code gọn gàng hơn.
