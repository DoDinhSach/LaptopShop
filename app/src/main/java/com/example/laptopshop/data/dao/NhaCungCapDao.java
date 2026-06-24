package com.example.laptopshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.NhaCungCap;

import java.util.ArrayList;

public class NhaCungCapDao {

    private final DBHelper dbHelper;

    public NhaCungCapDao(Context context) {
        this.dbHelper = new DBHelper(context);
    }

    public long insert(NhaCungCap s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DBHelper.COL_S_NAME, s.name);
        v.put(DBHelper.COL_S_BRAND, s.brand);
        v.put(DBHelper.COL_S_PHONE, s.phone);
        v.put(DBHelper.COL_S_ADDRESS, s.address);
        return db.insert(DBHelper.TBL_SUPPLIERS, null, v);
    }

    public boolean update(NhaCungCap s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DBHelper.COL_S_NAME, s.name);
        v.put(DBHelper.COL_S_BRAND, s.brand);
        v.put(DBHelper.COL_S_PHONE, s.phone);
        v.put(DBHelper.COL_S_ADDRESS, s.address);
        return db.update(DBHelper.TBL_SUPPLIERS, v, DBHelper.COL_ID + "=?", new String[]{String.valueOf(s.id)}) > 0;
    }

    public boolean delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TBL_SUPPLIERS, DBHelper.COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public ArrayList<NhaCungCap> getAll(String keyword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT * FROM " + DBHelper.TBL_SUPPLIERS;
        String[] args = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " WHERE " + DBHelper.COL_S_NAME + " LIKE ? OR " + DBHelper.COL_S_BRAND + " LIKE ?";
            args = new String[]{"%" + keyword + "%", "%" + keyword + "%"};
        }
        sql += " ORDER BY " + DBHelper.COL_S_NAME + " ASC";
        Cursor c = db.rawQuery(sql, args);
        while (c.moveToNext()) {
            list.add(readSupplier(c));
        }
        c.close();
        return list;
    }

    public NhaCungCap getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DBHelper.TBL_SUPPLIERS + " WHERE " + DBHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
        NhaCungCap s = null;
        if (c.moveToFirst()) {
            s = readSupplier(c);
        }
        c.close();
        return s;
    }

    public boolean hasReceipts(long supplierId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + DBHelper.TBL_RECEIPTS + " WHERE " + DBHelper.COL_R_SUPPLIER_ID + "=? LIMIT 1", new String[]{String.valueOf(supplierId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private NhaCungCap readSupplier(Cursor c) {
        NhaCungCap s = new NhaCungCap();
        s.id = c.getLong(c.getColumnIndexOrThrow(DBHelper.COL_ID));
        s.name = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_S_NAME));
        s.brand = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_S_BRAND));
        s.phone = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_S_PHONE));
        s.address = c.getString(c.getColumnIndexOrThrow(DBHelper.COL_S_ADDRESS));
        return s;
    }
}
