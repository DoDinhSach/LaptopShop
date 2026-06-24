package com.example.laptopshop.data.model;

public class CheckoutInfo {

    public static final String PAYMENT_COD = "COD";
    public static final String PAYMENT_BANK_TRANSFER = "CHUYEN_KHOAN";
    public static final int SHIPPING_FEE = 30000;

    public String receiverName;
    public String receiverPhone;
    public String receiverAddress;
    public String paymentMethod;
    public String note;
    public String discountCode;
    public int discountAmount;
    public int shippingFee = SHIPPING_FEE;
    public int subtotal;
    public int totalAmount;

    public static String normalizeDiscountCode(String rawCode) {
        if (rawCode == null) return null;
        String normalized = rawCode.trim().toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

    public static int calculateDiscount(GiamGia voucher, int subtotal) {
        if (voucher == null || !voucher.isActive) return 0;
        
        // Check expiry
        if (voucher.expiryDate < System.currentTimeMillis()) return 0;
        
        // Check minimum order
        if (subtotal < voucher.minOrderValue) return 0;
        
        return voucher.discountAmount;
    }
}
