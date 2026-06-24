package com.example.laptopshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.NguoiDungDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.data.model.NguoiDung;
import com.example.laptopshop.ui.home.AdminHomeActivity;
import com.example.laptopshop.ui.home.CustomerHomeActivity;
import com.example.laptopshop.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private CheckBox cbRememberMe;
    private NguoiDungDao nguoiDungDao;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        nguoiDungDao = new NguoiDungDao(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            if (session.getUserId() > 0 && nguoiDungDao.getById(session.getUserId()) != null) {
                openHomeByRole(session.getRole());
                return;
            }
            session.clear();
        }

        prefillRememberedLogin();
        setupPasswordToggle();

        if (btnLogin != null) btnLogin.setOnClickListener(v -> doLogin());
        if (tvGoRegister != null) tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        if (tvForgotPassword != null) tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, R.string.auth_forgot_password_message, Toast.LENGTH_SHORT).show());
    }

    private void setupPasswordToggle() {
        if (edtPassword == null) return;
        edtPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() != android.view.MotionEvent.ACTION_UP) {
                return false;
            }
            if (edtPassword.getCompoundDrawablesRelative()[2] == null) {
                return false;
            }

            int drawableWidth = edtPassword.getCompoundDrawablesRelative()[2].getBounds().width();
            int touchStart = edtPassword.getWidth() - edtPassword.getPaddingEnd() - drawableWidth;
            if (event.getX() < touchStart) {
                return false;
            }

            boolean currentlyHidden = edtPassword.getTransformationMethod() instanceof PasswordTransformationMethod;
            if (currentlyHidden) {
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            edtPassword.setSelection(edtPassword.getText().length());
            return true;
        });
    }

    private void prefillRememberedLogin() {
        if (edtUsername == null || !session.shouldRememberLogin()) {
            return;
        }
        if (cbRememberMe != null) cbRememberMe.setChecked(true);
        String rememberedUsername = session.getRememberedUsername();
        if (rememberedUsername != null) {
            edtUsername.setText(rememberedUsername);
            edtUsername.setSelection(rememberedUsername.length());
        }
    }

    private void doLogin() {
        if (edtUsername == null || edtPassword == null) return;
        String rawUsername = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(rawUsername) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String username = normalizeCredential(rawUsername);
        NguoiDung user = nguoiDungDao.dangNhap(username, password);
        if (user == null) {
            Toast.makeText(this, R.string.login_inactive_or_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        session.save(user.id, user.username, user.role);
        session.saveRememberedLogin(cbRememberMe != null && cbRememberMe.isChecked(), user.username);
        openHomeByRole(user.role);
    }

    private String normalizeCredential(String credential) {
        if (credential == null) {
            return "";
        }
        String value = credential.trim();
        if (value.contains("@")) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value;
    }

    private void openHomeByRole(String role) {
        Intent intent;

        if (DBHelper.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminHomeActivity.class);
        } else if (DBHelper.ROLE_CUSTOMER.equals(role)) {
            intent = new Intent(this, CustomerHomeActivity.class);
        } else {
            session.clear();
            Toast.makeText(this, R.string.invalid_role, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
        finish();
    }
}
