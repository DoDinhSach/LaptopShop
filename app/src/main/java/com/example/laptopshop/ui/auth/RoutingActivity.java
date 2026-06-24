package com.example.laptopshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.ui.home.AdminHomeActivity;
import com.example.laptopshop.ui.home.CustomerHomeActivity;
import com.example.laptopshop.utils.SessionManager;

public class RoutingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager session = new SessionManager(this);
        
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            String role = session.getRole();
            if (DBHelper.ROLE_ADMIN.equals(role)) {
                startActivity(new Intent(this, AdminHomeActivity.class));
            } else {
                startActivity(new Intent(this, CustomerHomeActivity.class));
            }
        }
        finish();
    }
}
