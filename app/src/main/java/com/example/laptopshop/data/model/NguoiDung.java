package com.example.laptopshop.data.model;

public class NguoiDung {
    public long id;
    public String fullname;
    public String username;
    public String role;
    public boolean isActive = true;
    public int orderCount;
    public int deliveredSpend;

    public NguoiDung(long id, String fullname, String username, String role) {
        this.id = id;
        this.fullname = fullname;
        this.username = username;
        this.role = role;
    }
}
