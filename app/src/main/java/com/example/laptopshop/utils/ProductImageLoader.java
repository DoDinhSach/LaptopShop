package com.example.laptopshop.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.laptopshop.R;

import java.util.Locale;

public final class ProductImageLoader {

    private ProductImageLoader() {
    }

    public static void load(ImageView imageView, String imageRef, String productName, String brand) {
        Context context = imageView.getContext();
        int fallbackRes = resolveFallbackImage(context, imageRef, productName, brand);
        if (isLoadableUri(imageRef)) {
            Glide.with(imageView)
                    .load(imageRef.trim())
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .into(imageView);
            return;
        }
        imageView.setImageResource(fallbackRes);
    }

    public static int resolveFallbackImage(Context context, String imageRef, String productName, String brand) {
        int imageRes = findImageRes(context, imageRef);
        if (imageRes != 0) {
            return imageRes;
        }

        String name = productName == null ? "" : productName.toLowerCase(Locale.ROOT);
        String normalizedBrand = brand == null ? "" : brand.toLowerCase(Locale.ROOT);

        // MacBook Fallback
        if (name.contains("macbook") || brandContains(normalizedBrand, "apple")) {
            int macRes = findImageRes(context, "m1");
            if (macRes != 0) return macRes;
        }
        
        // Dell Fallback
        if (name.contains("dell") || brandContains(normalizedBrand, "dell")) {
            int dellRes = findImageRes(context, "dell_xps");
            if (dellRes != 0) return dellRes;
        }

        // Asus Fallback
        if (name.contains("asus") || brandContains(normalizedBrand, "asus")) {
            int asusRes = findImageRes(context, "asus_zenbook");
            if (asusRes != 0) return asusRes;
        }

        return android.R.drawable.ic_menu_gallery;
    }

    public static boolean isInvalidImageInput(String imageRef) {
        if (imageRef == null) {
            return false;
        }
        String trimmed = imageRef.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (!looksLikeUri(trimmed)) {
            return false;
        }
        Uri uri = Uri.parse(trimmed);
        String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            return false;
        }
        String host = uri.getHost();
        return !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || host == null
                || host.trim().isEmpty()
                || trimmed.contains(" ");
    }

    private static boolean isLoadableUri(String imageRef) {
        if (isInvalidImageInput(imageRef)) return false;
        String trimmed = imageRef == null ? "" : imageRef.trim();
        return looksLikeUrl(trimmed) || trimmed.regionMatches(true, 0, "content://", 0, 10);
    }

    private static boolean looksLikeUri(String value) {
        return looksLikeUrl(value) || value.regionMatches(true, 0, "content://", 0, 10);
    }

    private static boolean looksLikeUrl(String value) {
        return value.regionMatches(true, 0, "http://", 0, 7)
                || value.regionMatches(true, 0, "https://", 0, 8);
    }

    private static boolean brandContains(String brand, String keyword) {
        return brand.contains(keyword);
    }

    private static int findImageRes(Context context, String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return 0;
        }

        String imageKey = imageName.trim();
        int resId = context.getResources().getIdentifier(imageKey, "drawable", context.getPackageName());
        if (resId != 0) {
            return resId;
        }
        return context.getResources().getIdentifier(imageKey, "mipmap", context.getPackageName());
    }
}
