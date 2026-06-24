package com.example.laptopshop.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.laptopshop.data.model.PhieuNhap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "phonestore.db";
    public static final int DB_VERSION = 22; 

    // ===== USERS  =====
    public static final String TBL_USERS = "nguoi_dung";
    public static final String COL_ID = "id";
    public static final String COL_FULLNAME = "ho_ten";
    public static final String COL_USERNAME = "ten_dang_nhap";
    public static final String COL_PASSWORD = "mat_khau";
    public static final String COL_ROLE = "vai_tro";
    public static final String COL_IS_ACTIVE = "is_active";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // ===== VOUCHERS =====
    public static final String TBL_VOUCHERS = "ma_giam_gia";
    public static final String COL_V_CODE = "ma_code";
    public static final String COL_V_DISCOUNT_AMOUNT = "so_tien_giam";
    public static final String COL_V_MIN_ORDER = "gia_tri_toi_thieu";
    public static final String COL_V_PRODUCT_ID = "san_pham_id";
    public static final String COL_V_EXPIRY = "ngay_het_han";

    // ===== PRODUCTS =====
    public static final String TBL_PRODUCTS = "san_pham";
    public static final String COL_P_NAME = "ten_san_pham";
    public static final String COL_P_BRAND = "hang";
    public static final String COL_P_PRICE = "gia";
    public static final String COL_P_STOCK = "ton_kho";
    public static final String COL_P_DISCOUNT = "giam_gia";
    public static final String COL_P_DESC = "mo_ta";
    public static final String COL_P_IMAGE = "ten_anh";
    public static final String COL_P_OS = "he_dieu_hanh";
    public static final String COL_P_ROM_GB = "rom_gb";
    public static final String COL_P_RAM_GB = "ram_gb";
    public static final String COL_P_CHIPSET = "chipset";
    public static final String COL_P_SCREEN = "man_hinh";
    public static final String COL_P_CAMERA = "camera";
    public static final String COL_P_BATTERY = "pin_mah";
    public static final String COL_P_COLORS = "mau_sac";
    public static final String COL_P_TYPE = "loai_san_pham";
    public static final String COL_P_REFRESH_RATE = "tan_so_quet";
    public static final String COL_P_RESOLUTION = "do_phan_giai";
    public static final String COL_P_RATING = "danh_gia";
    public static final String COL_P_SOLD = "da_ban";

    // ===== CART =====
    public static final String TBL_CART = "gio_hang";
    public static final String COL_C_USER_ID = "nguoi_dung_id";
    public static final String COL_C_PRODUCT_ID = "san_pham_id";
    public static final String COL_C_QTY = "so_luong";
    public static final String COL_C_STORAGE = "dung_luong";
    public static final String COL_C_COLOR = "mau_sac_chon";

    // ===== ORDERS (hoa_don) =====
    public static final String TBL_ORDERS = "hoa_don";
    public static final String COL_O_USER_ID = "nguoi_dung_id";
    public static final String COL_O_TOTAL = "tong_tien";
    public static final String COL_O_CREATED = "ngay_tao";
    public static final String COL_O_ORDER_STATUS = "trang_thai_don";
    public static final String COL_O_PAYMENT_STATUS = "trang_thai_thanh_toan";

    public static final String COL_O_RECEIVER = "nguoi_nhan";
    public static final String COL_O_PHONE = "sdt_nhan";
    public static final String COL_O_ADDRESS = "dia_chi_nhan";
    public static final String COL_O_PAY_METHOD = "phuong_thuc_thanh_toan";
    public static final String COL_O_NOTE = "ghi_chu";
    public static final String COL_O_SUBTOTAL = "tam_tinh";
    public static final String COL_O_SHIPPING_FEE = "phi_van_chuyen";
    public static final String COL_O_DISCOUNT_CODE = "ma_giam_gia";
    public static final String COL_O_DISCOUNT_AMOUNT = "tien_giam";
    public static final String COL_O_PAYMENT_DEADLINE = "payment_deadline";
    public static final String COL_O_EXPIRED_AT = "expired_at";
    public static final String COL_O_CANCELLED_AT = "cancelled_at";
    public static final String COL_O_CANCEL_REASON = "cancel_reason";
    public static final String COL_O_REFUND_STATUS = "refund_status";
    public static final String COL_O_REFUNDED_AT = "refunded_at";
    public static final String COL_O_REFUND_NOTE = "refund_note";

    public static final String TBL_ORDER_ITEMS = "hoa_don_ct";
    public static final String COL_OI_ORDER_ID = "hoa_don_id";
    public static final String COL_OI_PRODUCT_ID = "san_pham_id";
    public static final String COL_OI_NAME = "ten_san_pham";
    public static final String COL_OI_PRICE = "don_gia";
    public static final String COL_OI_DISCOUNT = "giam_gia";
    public static final String COL_OI_QTY = "so_luong";
    public static final String COL_OI_AMOUNT = "thanh_tien";
    public static final String COL_OI_STORAGE = "dung_luong";
    public static final String COL_OI_COLOR = "mau_sac_chon";

    // ===== SUPPLIERS / RECEIPTS / INVENTORY HISTORY =====
    public static final String TBL_SUPPLIERS = "nha_cung_cap";
    public static final String COL_S_NAME = "ten_nha_cung_cap";
    public static final String COL_S_BRAND = "hang";
    public static final String COL_S_PHONE = "so_dien_thoai";
    public static final String COL_S_ADDRESS = "dia_chi";

    public static final String TBL_RECEIPTS = "phieu_nhap";
    public static final String COL_R_SUPPLIER_ID = "nha_cung_cap_id";
    public static final String COL_R_TOTAL_QTY = "tong_so_luong";
    public static final String COL_R_TOTAL_AMOUNT = "tong_tien";
    public static final String COL_R_CREATED = "ngay_tao";
    public static final String COL_R_NOTE = "ghi_chu";
    public static final String COL_R_STATUS = "trang_thai";
    public static final String COL_R_CREATED_BY = "nguoi_tao";

    public static final String TBL_RECEIPT_ITEMS = "phieu_nhap_ct";
    public static final String COL_RI_RECEIPT_ID = "phieu_nhap_id";
    public static final String COL_RI_PRODUCT_ID = "san_pham_id";
    public static final String COL_RI_PRODUCT_NAME = "ten_san_pham";
    public static final String COL_RI_QTY = "so_luong";
    public static final String COL_RI_UNIT_COST = "don_gia_nhap";
    public static final String COL_RI_AMOUNT = "thanh_tien";

    public static final String TBL_INVENTORY_HISTORY = "lich_su_kho";
    public static final String COL_H_PRODUCT_ID = "san_pham_id";
    public static final String COL_H_PRODUCT_NAME = "ten_san_pham";
    public static final String COL_H_ACTION = "loai_bien_dong";
    public static final String COL_H_QTY = "so_luong";
    public static final String COL_H_REF_TYPE = "loai_tham_chieu";
    public static final String COL_H_REF_ID = "tham_chieu_id";
    public static final String COL_H_NOTE = "ghi_chu";
    public static final String COL_H_CREATED = "ngay_tao";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FULLNAME + " TEXT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_ROLE + " TEXT NOT NULL, " +
                COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1" +
                ");");

        ContentValues admin = new ContentValues();
        admin.put(COL_FULLNAME, "Admin");
        admin.put(COL_USERNAME, "admin");
        admin.put(COL_PASSWORD, "admin123");
        admin.put(COL_ROLE, ROLE_ADMIN);
        admin.put(COL_IS_ACTIVE, 1);
        db.insert(TBL_USERS, null, admin);

        ContentValues customer = new ContentValues();
        customer.put(COL_FULLNAME, "Khachhang");
        customer.put(COL_USERNAME, "khachhang");
        customer.put(COL_PASSWORD, "khachhang123");
        customer.put(COL_ROLE, ROLE_CUSTOMER);
        customer.put(COL_IS_ACTIVE, 1);
        db.insert(TBL_USERS, null, customer);

        ContentValues user1 = new ContentValues();
        user1.put(COL_FULLNAME, "Sach");
        user1.put(COL_USERNAME, "sach");
        user1.put(COL_PASSWORD, "sach123");
        user1.put(COL_ROLE, ROLE_CUSTOMER);
        user1.put(COL_IS_ACTIVE, 1);
        db.insert(TBL_USERS, null, user1);

        ContentValues user2 = new ContentValues();
        user2.put(COL_FULLNAME, "Binh");
        user2.put(COL_USERNAME, "binh");
        user2.put(COL_PASSWORD, "binh123");
        user2.put(COL_ROLE, ROLE_CUSTOMER);
        user2.put(COL_IS_ACTIVE, 1);
        db.insert(TBL_USERS, null, user2);

        db.execSQL("CREATE TABLE " + TBL_PRODUCTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_BRAND + " TEXT, " +
                COL_P_PRICE + " INTEGER NOT NULL, " +
                COL_P_STOCK + " INTEGER NOT NULL DEFAULT 0, " +
                COL_P_DISCOUNT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_P_DESC + " TEXT, " +
                COL_P_IMAGE + " TEXT, " +
                COL_P_OS + " TEXT, " +
                COL_P_ROM_GB + " INTEGER NOT NULL DEFAULT 0, " +
                COL_P_RAM_GB + " INTEGER NOT NULL DEFAULT 0, " +
                COL_P_CHIPSET + " TEXT, " +
                COL_P_SCREEN + " TEXT, " +
                COL_P_CAMERA + " TEXT, " +
                COL_P_BATTERY + " INTEGER NOT NULL DEFAULT 0, " +
                COL_P_COLORS + " TEXT, " +
                COL_P_TYPE + " TEXT, " +
                COL_P_REFRESH_RATE + " INTEGER NOT NULL DEFAULT 60, " +
                COL_P_RESOLUTION + " TEXT, " +
                COL_P_RATING + " REAL NOT NULL DEFAULT 0, " +
                COL_P_SOLD + " INTEGER NOT NULL DEFAULT 0, " +
                COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_CART + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_C_USER_ID + " INTEGER NOT NULL, " +
                COL_C_PRODUCT_ID + " INTEGER NOT NULL, " +
                COL_C_QTY + " INTEGER NOT NULL DEFAULT 1, " +
                COL_C_STORAGE + " TEXT, " +
                COL_C_COLOR + " TEXT, " +
                "UNIQUE(" + COL_C_USER_ID + "," + COL_C_PRODUCT_ID + "," + COL_C_STORAGE + "," + COL_C_COLOR + "), " +
                "FOREIGN KEY(" + COL_C_USER_ID + ") REFERENCES " + TBL_USERS + "(" + COL_ID + "), " +
                "FOREIGN KEY(" + COL_C_PRODUCT_ID + ") REFERENCES " + TBL_PRODUCTS + "(" + COL_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_ORDERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_O_USER_ID + " INTEGER NOT NULL, " +
                COL_O_TOTAL + " INTEGER NOT NULL, " +
                COL_O_CREATED + " INTEGER NOT NULL, " +
                COL_O_ORDER_STATUS + " TEXT NOT NULL, " +
                COL_O_PAYMENT_STATUS + " TEXT NOT NULL, " +
                COL_O_RECEIVER + " TEXT, " +
                COL_O_PHONE + " TEXT, " +
                COL_O_ADDRESS + " TEXT, " +
                COL_O_PAY_METHOD + " TEXT, " +
                COL_O_NOTE + " TEXT, " +
                COL_O_SUBTOTAL + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_SHIPPING_FEE + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_DISCOUNT_CODE + " TEXT, " +
                COL_O_DISCOUNT_AMOUNT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_PAYMENT_DEADLINE + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_EXPIRED_AT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_CANCELLED_AT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_CANCEL_REASON + " TEXT, " +
                COL_O_REFUND_STATUS + " TEXT, " +
                COL_O_REFUNDED_AT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_O_REFUND_NOTE + " TEXT, " +
                "FOREIGN KEY(" + COL_O_USER_ID + ") REFERENCES " + TBL_USERS + "(" + COL_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_ORDER_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OI_ORDER_ID + " INTEGER NOT NULL, " +
                COL_OI_PRODUCT_ID + " INTEGER NOT NULL, " +
                COL_OI_NAME + " TEXT NOT NULL, " +
                COL_OI_PRICE + " INTEGER NOT NULL, " +
                COL_OI_DISCOUNT + " INTEGER NOT NULL DEFAULT 0, " +
                COL_OI_QTY + " INTEGER NOT NULL, " +
                COL_OI_AMOUNT + " INTEGER NOT NULL, " +
                COL_OI_STORAGE + " TEXT, " +
                COL_OI_COLOR + " TEXT, " +
                "FOREIGN KEY(" + COL_OI_ORDER_ID + ") REFERENCES " + TBL_ORDERS + "(" + COL_ID + ") ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_SUPPLIERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_S_NAME + " TEXT NOT NULL, " +
                COL_S_BRAND + " TEXT, " +
                COL_S_PHONE + " TEXT, " +
                COL_S_ADDRESS + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_RECEIPTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_SUPPLIER_ID + " INTEGER NOT NULL, " +
                COL_R_TOTAL_QTY + " INTEGER NOT NULL, " +
                COL_R_TOTAL_AMOUNT + " INTEGER NOT NULL, " +
                COL_R_CREATED + " INTEGER NOT NULL, " +
                COL_R_NOTE + " TEXT, " +
                COL_R_STATUS + " TEXT NOT NULL DEFAULT '" + PhieuNhap.STATUS_DRAFT + "', " +
                COL_R_CREATED_BY + " TEXT, " +
                "FOREIGN KEY(" + COL_R_SUPPLIER_ID + ") REFERENCES " + TBL_SUPPLIERS + "(" + COL_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_RECEIPT_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECEIPT_ID + " INTEGER NOT NULL, " +
                COL_RI_PRODUCT_ID + " INTEGER NOT NULL, " +
                COL_RI_PRODUCT_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " INTEGER NOT NULL, " +
                COL_RI_UNIT_COST + " INTEGER NOT NULL, " +
                COL_RI_AMOUNT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECEIPT_ID + ") REFERENCES " + TBL_RECEIPTS + "(" + COL_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_RI_PRODUCT_ID + ") REFERENCES " + TBL_PRODUCTS + "(" + COL_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_INVENTORY_HISTORY + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_H_PRODUCT_ID + " INTEGER NOT NULL, " +
                COL_H_PRODUCT_NAME + " TEXT NOT NULL, " +
                COL_H_ACTION + " TEXT NOT NULL, " +
                COL_H_QTY + " INTEGER NOT NULL, " +
                COL_H_REF_TYPE + " TEXT, " +
                COL_H_REF_ID + " INTEGER, " +
                COL_H_NOTE + " TEXT, " +
                COL_H_CREATED + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_H_PRODUCT_ID + ") REFERENCES " + TBL_PRODUCTS + "(" + COL_ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + TBL_VOUCHERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_V_CODE + " TEXT UNIQUE NOT NULL, " +
                COL_V_DISCOUNT_AMOUNT + " INTEGER NOT NULL, " +
                COL_V_MIN_ORDER + " INTEGER NOT NULL DEFAULT 0, " +
                COL_V_PRODUCT_ID + " INTEGER, " +
                COL_V_EXPIRY + " INTEGER NOT NULL, " +
                COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(" + COL_V_PRODUCT_ID + ") REFERENCES " + TBL_PRODUCTS + "(" + COL_ID + ") ON DELETE SET NULL" +
                ");");

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Suppliers
        long appleId = seedSupplier(db, "Apple Vietnam Supply", "Apple", "0900000001", "TP.HCM");
        long asusId = seedSupplier(db, "Asus Vietnam", "Asus", "0900000002", "Hà Nội");
        long dellId = seedSupplier(db, "Dell Vietnam", "Dell", "0900000003", "Đà Nẵng");
        long hpId = seedSupplier(db, "HP Vietnam", "HP", "0900000004", "TP.HCM");
        long msiId = seedSupplier(db, "MSI Global", "MSI", "0900000005", "Đài Loan");

        // Apple (MacBook Air)
        long m1Id = seedProduct(db, "MacBook Air M1", "Apple", 18490000, 10, 5,
                "Chip M1 bền bỉ, mỏng nhẹ, pin ấn tượng cho sinh viên.", "m1",
                "MacOS", 256, 8, "Apple M1", "13.3 inch", "720p FaceTime HD", 50,
                "Space Gray,Silver,Gold", "MacBook Air", 60, "2.8K", 4.8f, 500);
        seedProduct(db, "MacBook Air M2", "Apple", 24990000, 8, 5,
                "Thiết kế mới, chip M2 mạnh mẽ, màn hình Liquid Retina.", "m2",
                "MacOS", 512, 16, "Apple M2", "13.6 inch", "1080p FaceTime HD", 52,
                "Midnight,Starlight,Space Gray,Silver", "MacBook Air", 60, "2.8K", 4.9f, 300);
        seedProduct(db, "MacBook Air M3", "Apple", 27990000, 15, 0,
                "Hiệu năng vượt trội with chip M3, hỗ trợ xuất 2 màn hình rời.", "m3",
                "MacOS", 512, 16, "Apple M3", "13.6 inch", "1080p FaceTime HD", 52,
                "Midnight,Starlight,Space Gray,Silver", "MacBook Air", 60, "2.8K", 4.9f, 150);
        seedProduct(db, "MacBook Air M4", "Apple", 32990000, 5, 0,
                "Thế hệ mới nhất with chip M4 cực khủng, xử lý AI mượt mà.", "m4",
                "MacOS", 512, 16, "Apple M4", "13.6 inch", "1080p FaceTime HD", 52,
                "Midnight,Space Gray", "MacBook Air", 60, "2560x1664", 5.0f, 50);

        // Asus (Gaming & Thin-Light)
        seedProduct(db, "Asus Zenbook 14 OLED", "Asus", 28990000, 7, 10,
                "Màn hình OLED 3K, Intel Core Ultra 7 siêu mạnh.", "asus_zenbook",
                "Windows", 512, 16, "Intel Core Ultra 7", "14 inch", "FHD Camera", 75,
                "Ponder Blue", "Laptop Mỏng Nhẹ", 120, "3K", 4.7f, 120);
        seedProduct(db, "Asus Vivobook 15", "Asus", 12490000, 20, 8,
                "Laptop văn phòng quốc dân, bền bỉ, màn hình lớn.", "asus_vivobook",
                "Windows", 512, 8, "Intel Core i5", "15.6 inch", "HD Camera", 42,
                "Quiet Blue", "Laptop Văn Phòng", 60, "Full HD (1920x1080)", 4.5f, 400);
        seedProduct(db, "Asus TUF Gaming F15", "Asus", 18990000, 12, 15,
                "Chiến game cực đỉnh with dòng TUF bền bỉ.", "asus_tuff",
                "Windows", 512, 16, "Intel Core i7", "15.6 inch", "HD Camera", 48,
                "Graphite Black", "Laptop Gaming", 144, "Full HD (1920x1080)", 4.6f, 250);

        // Dell (Business & Precision)
        seedProduct(db, "Dell Precision 5760", "Dell", 55990000, 2, 0,
                "Trạm làm việc di động cực khủng i9 Workstation.", "dell_precision",
                "Windows", 1024, 64, "Intel Core i9", "17 inch trở lên", "IR Camera", 97,
                "Titan Gray", "Laptop Doanh Nhân", 60, "4K UHD (3840x2160)", 4.9f, 15);
        seedProduct(db, "Dell XPS 13", "Dell", 25490000, 6, 7,
                "Siêu phẩm mỏng nhẹ nhất dòng XPS, màn hình vô cực.", "dell_xps",
                "Windows", 512, 16, "Intel Core i5", "Dưới 14 inch", "FHD Camera", 51,
                "Sky Blue", "Laptop Doanh Nhân", 60, "Full HD (1920x1080)", 4.8f, 80);

        // HP (OmniBook & Stream)
        seedProduct(db, "HP OmniBook X", "HP", 35990000, 4, 5,
                "Dòng AI PC mới với Snapdragon X Elite, pin 26 tiếng.", "hp_ommibook",
                "Windows", 1024, 32, "Snapdragon X Elite", "14 inch", "5MP Camera", 68,
                "Silver", "Laptop Mỏng Nhẹ", 60, "Full HD (1920x1080)", 4.8f, 30);
        seedProduct(db, "HP Stream 14", "HP", 5990000, 10, 0,
                "Laptop giá rẻ cho học sinh, mỏng nhẹ thời trang.", "hp_streeam",
                "Windows", 64, 4, "Intel Celeron", "14 inch", "Webcam", 41,
                "Blue,White", "Laptop Văn Phòng", 60, "HD (1366x768)", 4.0f, 100);

        // MSI (Titan & GF)
        seedProduct(db, "MSI Titan GT77 HX", "MSI", 125000000, 2, 0,
                "Siêu laptop gaming mạnh nhất thế giới, i9-13980HX.", "titan",
                "Windows", 2048, 64, "Intel Core i9", "17 inch trở lên", "FHD Camera", 99,
                "Core Black", "Laptop Gaming", 144, "4K UHD (3840x2160)", 5.0f, 5);
        seedProduct(db, "MSI GF63 Thin", "MSI", 16490000, 15, 10,
                "Laptop gaming quốc dân mỏng nhẹ, hiệu năng tốt.", "gf",
                "Windows", 512, 8, "Intel Core i5", "15.6 inch", "HD Camera", 51,
                "Black", "Laptop Gaming", 144, "Full HD (1920x1080)", 4.6f, 180);

        seedVoucher(db, "WELCOME10", 10000, 50000, null, System.currentTimeMillis() + 30L * 24 * 3600 * 1000);
        
        seedInitialReceipt(db, appleId, m1Id, "MacBook Air M1", 10, 16000000, "Nhập lô đầu tiên", PhieuNhap.STATUS_COMPLETED, "Admin");
        long dummy = asusId + dellId + hpId + msiId;
    }

    private long seedProduct(SQLiteDatabase db,
                             String tenSanPham, String hang, int gia,
                             int tonKho, int giamGia, String moTa, String tenAnh,
                             String heDieuHanh, int romGb, int ramGb, String chipset,
                             String manHinh, String camera, int pinMah, String mauSac,
                             String loaiSanPham, int tanSoQuet, String doPhanGiai,
                             float danhGia, int daBan) {
        ContentValues v = new ContentValues();
        v.put(COL_P_NAME, tenSanPham);
        v.put(COL_P_BRAND, hang);
        v.put(COL_P_PRICE, gia);
        v.put(COL_P_STOCK, tonKho);
        v.put(COL_P_DISCOUNT, giamGia);
        v.put(COL_P_DESC, moTa);
        v.put(COL_P_IMAGE, tenAnh);
        v.put(COL_P_OS, heDieuHanh);
        v.put(COL_P_ROM_GB, romGb);
        v.put(COL_P_RAM_GB, ramGb);
        v.put(COL_P_CHIPSET, chipset);
        v.put(COL_P_SCREEN, manHinh);
        v.put(COL_P_CAMERA, camera);
        v.put(COL_P_BATTERY, pinMah);
        v.put(COL_P_COLORS, mauSac);
        v.put(COL_P_TYPE, loaiSanPham);
        v.put(COL_P_REFRESH_RATE, tanSoQuet);
        v.put(COL_P_RESOLUTION, doPhanGiai);
        v.put(COL_P_RATING, danhGia);
        v.put(COL_P_SOLD, daBan);
        v.put(COL_IS_ACTIVE, 1);
        return db.insert(TBL_PRODUCTS, null, v);
    }

    private long seedSupplier(SQLiteDatabase db, String name, String brand, String phone, String address) {
        ContentValues v = new ContentValues();
        v.put(COL_S_NAME, name);
        v.put(COL_S_BRAND, brand);
        v.put(COL_S_PHONE, phone);
        v.put(COL_S_ADDRESS, address);
        return db.insert(TBL_SUPPLIERS, null, v);
    }

    private void seedVoucher(SQLiteDatabase db, String code, int amount, int minOrder, Long productId, long expiry) {
        ContentValues v = new ContentValues();
        v.put(COL_V_CODE, code);
        v.put(COL_V_DISCOUNT_AMOUNT, amount);
        v.put(COL_V_MIN_ORDER, minOrder);
        v.put(COL_V_PRODUCT_ID, productId);
        v.put(COL_V_EXPIRY, expiry);
        v.put(COL_IS_ACTIVE, 1);
        db.insert(TBL_VOUCHERS, null, v);
    }

    private void seedInitialReceipt(SQLiteDatabase db,
                                    long supplierId,
                                    long productId,
                                    String productName,
                                    int quantity,
                                    int unitCost,
                                    String note,
                                    String status,
                                    String creatorName) {
        long createdAt = System.currentTimeMillis();
        int totalAmount = quantity * unitCost;

        ContentValues receipt = new ContentValues();
        receipt.put(COL_R_SUPPLIER_ID, supplierId);
        receipt.put(COL_R_TOTAL_QTY, quantity);
        receipt.put(COL_R_TOTAL_AMOUNT, totalAmount);
        receipt.put(COL_R_CREATED, createdAt);
        receipt.put(COL_R_NOTE, note);
        receipt.put(COL_R_STATUS, status);
        receipt.put(COL_R_CREATED_BY, creatorName);
        long receiptId = db.insert(TBL_RECEIPTS, null, receipt);

        ContentValues receiptItem = new ContentValues();
        receiptItem.put(COL_RI_RECEIPT_ID, receiptId);
        receiptItem.put(COL_RI_PRODUCT_ID, productId);
        receiptItem.put(COL_RI_PRODUCT_NAME, productName);
        receiptItem.put(COL_RI_QTY, quantity);
        receiptItem.put(COL_RI_UNIT_COST, unitCost);
        receiptItem.put(COL_RI_AMOUNT, totalAmount);
        db.insert(TBL_RECEIPT_ITEMS, null, receiptItem);

        if (!PhieuNhap.STATUS_COMPLETED.equals(status)) {
            return;
        }

        db.execSQL(
                "UPDATE " + TBL_PRODUCTS +
                        " SET " + COL_P_STOCK + " = " + COL_P_STOCK + " + ?" +
                        " WHERE " + COL_ID + "=?",
                new Object[]{quantity, productId}
        );

        ContentValues history = new ContentValues();
        history.put(COL_H_PRODUCT_ID, productId);
        history.put(COL_H_PRODUCT_NAME, productName);
        history.put(COL_H_ACTION, "IMPORT");
        history.put(COL_H_QTY, quantity);
        history.put(COL_H_REF_TYPE, "RECEIPT");
        history.put(COL_H_REF_ID, receiptId);
        history.put(COL_H_NOTE, note);
        history.put(COL_H_CREATED, createdAt);
        db.insert(TBL_INVENTORY_HISTORY, null, history);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 22) {
            db.execSQL("PRAGMA foreign_keys=OFF;");
            db.execSQL("DROP TABLE IF EXISTS " + TBL_ORDER_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_RECEIPT_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_CART);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_INVENTORY_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_RECEIPTS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_SUPPLIERS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_VOUCHERS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_USERS);
            db.execSQL("PRAGMA foreign_keys=ON;");
            onCreate(db);
        }
    }
}
