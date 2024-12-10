package com.example.pothole;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Giao diện của SplashActivity

        // Tạo Handler để delay 2 giây
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                // Nếu đã đăng nhập, chuyển đến MapActivity
                intent = new Intent(SplashActivity.this, MapActivity.class);
            } else {
                // Nếu chưa đăng nhập, chuyển đến LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("language", "en"); // "en" là ngôn ngữ mặc định
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

}
