package com.example.laptopshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.laptopshop.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start floating animations for brand stickers with shadows
        applyFloatingAnimation(findViewById(R.id.cardStickerDell), R.anim.bubble_float_medium, 0L);
        applyFloatingAnimation(findViewById(R.id.cardStickerAsus), R.anim.bubble_float_slow, 200L);
        applyFloatingAnimation(findViewById(R.id.cardStickerLenovo), R.anim.bubble_float_fast, 400L);
        applyFloatingAnimation(findViewById(R.id.cardStickerHP), R.anim.bubble_float_medium, 600L);
        applyFloatingAnimation(findViewById(R.id.cardStickerMSI), R.anim.bubble_float_slow, 800L);
        applyFloatingAnimation(findViewById(R.id.cardStickerApple), R.anim.bubble_float_fast, 1000L);

        findViewById(R.id.btnGoLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void applyFloatingAnimation(View view, int animRes, long startOffset) {
        if (view == null) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, animRes);
        animation.setStartOffset(startOffset);
        view.startAnimation(animation);
    }
}
