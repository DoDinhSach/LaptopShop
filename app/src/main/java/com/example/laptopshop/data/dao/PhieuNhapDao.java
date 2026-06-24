package com.example.laptopshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.SanPham;
import com.example.laptopshop.data.model.PhieuNhap;
import com.example.laptopshop.data.model.PhieuNhapItem;
import com.example.laptopshop.data.model.Supplier;

import java.util.ArrayList;

public class PhieuNhapDao {

    private static final String REFERENCE_TYPE_RECEIPT = "RECEIPT";
    private static final String DEFAULT_CREATOR = "Admin";

    private final Context context;
    private final DBHelper dbHelper;
    private final InventoryHistoryDao historyDao;

    public PhieuNhapDao(Context ctx) {
        context = ctx;
        dbHelper = new DBHelper(ctx);
        historyDao = new InventoryHistoryDao(ctx);
    }

    public long saveDraftReceipt(long supplierId, String note, String creatorName, ArrayList<PhieuNhapItem> items) {
        return insertReceipt(supplierId, note, creatorName, PhieuNhap.STATUS_DRAFT, items, false);
    }

    public long createConfirmedReceipt(long supplierId, String note, String creatorName, ArrayList<PhieuNhapItem> items) {
        return insertReceipt(supplierId, note, creatorName, PhieuNhap.STATUS_COMPLETED, items, true);
    }

    public boolean confirmDraftReceipt(long receiptId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            PhieuNhap receipt = getReceiptById(db, receiptId);
            if (receipt == null || !receipt.isDraft()) {
                return false;
            }

            Supplier supplier = new SupplierDao(context).getById(receipt.supplierId);
            if (supplier == null) {
                return false;
            }

            ArrayList<PhieuNhapItem> storedItems = getReceiptItems(db, receiptId);
            ArrayList<PhieuNhapItem> normalizedItems = normalizeItems(storedItems, supplier, new SanPhamDao(context));
            if (normalizedItems.isEmpty() || normalizedItems.size() != storedItems.size()) {
                return false;
            }

            int totalQty = 0;
            int totalAmount = 0;
            for (PhieuNhapItem item : normalizedItems) {
                totalQty += item.quantity;
                totalAmount += item.amount;
            }

            ContentValues receiptValues = new ContentValues();
            receiptValues.put(DBHelper.COL_R_TOTAL_QTY, totalQty);
            receiptValues.put(DBHelper.COL_R_TOTAL_AMOUNT, totalAmount);
            receiptValues.put(DBHelper.COL_R_STATUS, PhieuNhap.STATUS_COMPLETED);
            int updated = db.update(
                    DBHelper.TBL_RECEIPTS,
                    receiptValues,
                    DBHelper.COL_ID + "=? AND " + DBHelper.COL_R_STATUS + "=?",
                    new String[]{String.valueOf(receiptId), PhieuNhap.STATUS_DRAFT}
            );
            if (updated <= 0) {
                return false;
            }

            applyInventoryImport(db, receiptId, normalizeNote(receipt.note), normalizedItems);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public PhieuNhap getReceiptById(long receiptId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getReceiptById(db, receiptId);
    }

    public ArrayList<PhieuNhap> getRecentReceipts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<PhieuNhap> list = new ArrayList<>();
        Cursor c = db.rawQuery(buildReceiptQuery(null), null);
        while (c.moveToNext()) {
            list.add(readReceipt(c));
        }
        c.close();
        return list;
    }

    public ArrayList<PhieuNhapItem> getReceiptItems(long receiptId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getReceiptItems(db, receiptId);
    }

    public boolean deleteReceipt(long receiptId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            PhieuNhap receipt = getReceiptById(db, receiptId);
            if (receipt == null || receipt.isCompleted()) {
                return false;
            }

            int deleted = db.delete(DBHelper.TBL_RECEIPTS, DBHelper.COL_ID + "=?", new String[]{String.valueOf(receiptId)});
            db.setTransactionSuccessful();
            return deleted > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean hasReceiptForSupplier(long supplierId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + DBHelper.TBL_RECEIPTS + " WHERE " + DBHelper.COL_R_SUPPLIER_ID + "=? LIMIT 1",
                new String[]{String.valueOf(supplierId)}
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public int countReceipts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TBL_RECEIPTS, null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    private long insertReceipt(long supplierId,
                               String note,
                               String creatorName,
                               String status,
                               ArrayList<PhieuNhapItem> items,
                               boolean applyInventory) {
        if (items == null || items.isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            SupplierDao supplierDao = new SupplierDao(context);
            SanPhamDao productDao = new SanPhamDao(context);
            Supplier supplier = supplierDao.getById(supplierId);
            if (supplier == null) {
                return -1;
            }

            ArrayList<PhieuNhapItem> normalizedItems = normalizeItems(items, supplier, productDao);
            if (normalizedItems.isEmpty()) {
                return -1;
            }

            int totalQty = 0;
            int totalAmount = 0;
            for (PhieuNhapItem item : normalizedItems) {
                totalQty += item.quantity;
                totalAmount += item.amount;
            }

            ContentValues receiptValues = new ContentValues();
            receiptValues.put(DBHelper.COL_R_SUPPLIER_ID, supplierId);
            receiptValues.put(DBHelper.COL_R_TOTAL_QTY, totalQty);
            receiptValues.put(DBHelper.COL_R_TOTAL_AMOUNT, totalAmount);
            receiptValues.put(DBHelper.COL_R_CREATED, System.currentTimeMillis());
            receiptValues.put(DBHelper.COL_R_NOTE, normalizeNote(note));
            receiptValues.put(DBHelper.COL_R_STATUS, normalizeStatus(status));
            receiptValues.put(DBHelper.COL_R_CREATED_BY, normalizeCreator(creatorName));
            long receiptId = db.insert(DBHelper.TBL_RECEIPTS, null, receiptValues);
            if (receiptId == -1) {
                return -1;
            }

            for (PhieuNhapItem item : normalizedItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(DBHelper.COL_RI_RECEIPT_ID, receiptId);
                itemValues.put(DBHelper.COL_RI_PRODUCT_ID, item.productId);
                itemValues.put(DBHelper.COL_RI_PRODUCT_NAME, item.productName);
                itemValues.put(DBHelper.COL_RI_QTY, item.quantity);
                itemValues.put(DBHelper.COL_RI_UNIT_COST, item.unitCost);
                itemValues.put(DBHelper.COL_RI_AMOUNT, item.amount);
                if (db.insert(DBHelper.TBL_RECEIPT_ITEMS, null, itemValues) == -1) {
                    return -1;
                }
            }

            if (applyInventory) {
                applyInventoryImport(db, receiptId, normalizeNote(note), normalizedItems);
            }

            db.setTransactionSuccessful();
            return receiptId;
        } finally {
            db.endTransaction();
        }
    }

    private void applyInventoryImport(SQLiteDatabase db,
                                      long receiptId,
                                      String note,
                                      ArrayList<PhieuNhapItem> items) {
        for (PhieuNhapItem item : items) {
            db.execSQL(
                    "UPDATE " + DBHelper.TBL_PRODUCTS +
                            " SET " + DBHelper.COL_P_STOCK + " = " + DBHelper.COL_P_STOCK + " + ?" +
                            " WHERE " + DBHelper.COL_ID + "=?",
                    new Object[]{item.quantity, item.productId}
            );

            historyDao.insert(
                    db,
                    item.productId,
                    item.productName,
                    InventoryHistoryDao.ACTION_IMPORT,
                    item.quantity,
                    REFERENCE_TYPE_RECEIPT,
                    receiptId,
                    note
            );
        }
    }

    private PhieuNhap getReceiptById(SQLiteDatabase db, long receiptId) {
        Cursor c = db.rawQuery(buildReceiptQuery(" WHERE r." + DBHelper.COL_ID + "=? LIMIT 1"), new String[]{String.valueOf(receiptId)});
        PhieuNhap receipt = null;
        if (c.moveToFirst()) {
            receipt = readReceipt(c);
        }
        c.close();
        return receipt;
    }

    private ArrayList<PhieuNhapItem> getReceiptItems(SQLiteDatabase db, long receiptId) {
        ArrayList<PhieuNhapItem> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DBHelper.TBL_RECEIPT_ITEMS +
                        " WHERE " + DBHelper.COL_RI_RECEIPT_ID + "=? ORDER BY " + DBHelper.COL_ID + " ASC",
                new String[]{String.valueOf(receiptId)}
        );
        while (c.moveToNext()) {
            list.add(readItem(c));
        }
        c.close();
        return list;
    }

    private String buildReceiptQuery(String suffix) {
        return "SELECT r." + DBHelper.COL_ID + "," +
                " r." + DBHelper.COL_R_SUPPLIER_ID + "," +
                " s." + DBHelper.COL_S_NAME + "," +
                " r." + DBHelper.COL_R_TOTAL_QTY + "," +
                " r." + DBHelper.COL_R_TOTAL_AMOUNT + "," +
                " r." + DBHelper.COL_R_CREATED + "," +
                " r." + DBHelper.COL_R_NOTE + "," +
                " r." + DBHelper.COL_R_STATUS + "," +
                " r." + DBHelper.COL_R_CREATED_BY + "," +
                " (SELECT COUNT(*) FROM " + DBHelper.TBL_RECEIPT_ITEMS + " ri WHERE ri." + DBHelper.COL_RI_RECEIPT_ID + " = r." + DBHelper.COL_ID + ") AS line_count" +
                " FROM " + DBHelper.TBL_RECEIPTS + " r" +
                " JOIN " + DBHelper.TBL_SUPPLIERS + " s ON s." + DBHelper.COL_ID + " = r." + DBHelper.COL_R_SUPPLIER_ID +
                (suffix == null ? " ORDER BY r." + DBHelper.COL_ID + " DESC" : suffix);
    }

    private ArrayList<PhieuNhapItem> normalizeItems(ArrayList<PhieuNhapItem> items, Supplier supplier, SanPhamDao productDao) {
        ArrayList<PhieuNhapItem> normalized = new ArrayList<>();
        if (supplier == null) {
            return normalized;
        }

        for (PhieuNhapItem item : items) {
            if (item == null) {
                continue;
            }
            if (item.productId <= 0 || item.quantity <= 0 || item.unitCost <= 0) {
                continue;
            }

            SanPham product = productDao.getById(item.productId, true);
            if (product == null || !product.isActive) {
                continue;
            }

            item.productName = product.tenSanPham == null ? "" : product.tenSanPham;
            item.recalculateAmount();
            normalized.add(item);
        }
        return normalized;
    }

    private String normalizeNote(String note) {
        if (note == null) {
            return "";
        }
        return note.trim();
    }

    private String normalizeCreator(String creatorName) {
        if (creatorName == null || creatorName.trim().isEmpty()) {
            return DEFAULT_CREATOR;
        }
        return creatorName.trim();
    }

    private String normalizeStatus(String status) {
        return PhieuNhap.STATUS_COMPLETED.equals(status) ? PhieuNhap.STATUS_COMPLETED : PhieuNhap.STATUS_DRAFT;
    }

    private PhieuNhap readReceipt(Cursor c) {
        PhieuNhap receipt = new PhieuNhap();
        receipt.id = c.getLong(0);
        receipt.supplierId = c.getLong(1);
        receipt.supplierName = c.getString(2);
        receipt.totalQuantity = c.getInt(3);
        receipt.totalAmount = c.getInt(4);
        receipt.createdAt = c.getLong(5);
        receipt.note = c.getString(6);
        receipt.status = c.getString(7);
        receipt.creatorName = c.getString(8);
        receipt.lineCount = c.getInt(9);
        return receipt;
    }

    private PhieuNhapItem readItem(Cursor c) {
        PhieuNhapItem item = new PhieuNhapItem();
        item.id = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_ID));
        item.receiptId = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_RI_RECEIPT_ID));
        item.productId = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_RI_PRODUCT_ID));
        item.productName = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_RI_PRODUCT_NAME));
        item.quantity = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_RI_QTY));
        item.unitCost = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_RI_UNIT_COST));
        item.amount = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_RI_AMOUNT));
        return item;
    }
}
