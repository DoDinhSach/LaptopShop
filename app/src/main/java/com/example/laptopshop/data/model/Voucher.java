package com.example.laptopshop.data.model;

public class Voucher {
    public long id;
    public String code;
    public int discountAmount;
    public int minOrderValue;
    public Long productId; // null if applicable to all products
    public long expiryDate;
    public boolean isActive = true;

    // Optional field for displaying product name in UI
    public String productName;
}
