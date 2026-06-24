package com.example.laptopshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.GiamGia;

import java.util.ArrayList;

public class GiamGiaDao {
    private final DBHelper dbHelper;

    public GiamGiaDao(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public long insert(GiamGia voucher) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(DBHelper.TBL_VOUCHERS, null, toValues(voucher));
    }

    public boolean update(GiamGia voucher) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(DBHelper.TBL_VOUCHERS, toValues(voucher),
                DBHelper.COL_ID + "=?", new String[]{String.valueOf(voucher.id)}) > 0;
    }

    public boolean delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TBL_VOUCHERS, DBHelper.COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public ArrayList<GiamGia> getAll() {
        ArrayList<GiamGia> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT v.*, p." + DBHelper.COL_P_NAME + 
                     " FROM " + DBHelper.TBL_VOUCHERS + " v " +
                     " LEFT JOIN " + DBHelper.TBL_PRODUCTS + " p ON v." + DBHelper.COL_V_PRODUCT_ID + " = p." + DBHelper.COL_ID +
                     " ORDER BY v." + DBHelper.COL_ID + " DESC";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            list.add(readVoucher(c));
        }
        c.close();
        return list;
    }

    public GiamGia getByCode(String code) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT v.*, p." + DBHelper.COL_P_NAME + 
                     " FROM " + DBHelper.TBL_VOUCHERS + " v " +
                     " LEFT JOIN " + DBHelper.TBL_PRODUCTS + " p ON v." + DBHelper.COL_V_PRODUCT_ID + " = p." + DBHelper.COL_ID +
                     " WHERE v." + DBHelper.COL_V_CODE + " = ? AND v." + DBHelper.COL_IS_ACTIVE + " = 1 LIMIT 1";
        Cursor c = db.rawQuery(sql, new String[]{code});
        GiamGia voucher = null;
        if (c.moveToFirst()) {
            voucher = readVoucher(c);
        }
        c.close();
        return voucher;
    }

    private ContentValues toValues(GiamGia v) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_V_CODE, v.code);
        values.put(DBHelper.COL_V_DISCOUNT_AMOUNT, v.discountAmount);
        values.put(DBHelper.COL_V_MIN_ORDER, v.minOrderValue);
        values.put(DBHelper.COL_V_PRODUCT_ID, v.productId);
        values.put(DBHelper.COL_V_EXPIRY, v.expiryDate);
        values.put(DBHelper.COL_IS_ACTIVE, v.isActive ? 1 : 0);
        return values;
    }

    private GiamGia readVoucher(Cursor c) {
        GiamGia v = new GiamGia();
        v.id = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_ID));
        v.code = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_V_CODE));
        v.discountAmount = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_V_DISCOUNT_AMOUNT));
        v.minOrderValue = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_V_MIN_ORDER));
        int prodIdIdx = c.getColumnIndexOrThrow(DBHelper.COL_V_PRODUCT_ID);
        if (!c.isNull(prodIdIdx)) {
            v.productId = c.getLong(prodIdIdx);
        }
        v.expiryDate = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_V_EXPIRY));
        v.isActive = c.getInt(c.getColumnIndexOrThrow(DBHelper.COL_IS_ACTIVE)) == 1;
        
        int nameIdx = c.getColumnIndex(DBHelper.COL_P_NAME);
        if (nameIdx >= 0) {
            v.productName = c.getString(nameIdx);
        }
        return v;
    }
}
