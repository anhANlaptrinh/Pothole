package com.example.pothole;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (savedInstanceState == null) {
            String openFragment = getIntent().getStringExtra("openFragment");
            if (openFragment != null) {
                switch (openFragment) {
                    case "about":
                        bottomNavigationView.setSelectedItemId(R.id.about);
                        replaceFragment(new AboutFragment());
                        break;
                    case "settings":
                        bottomNavigationView.setSelectedItemId(R.id.settings);
                        replaceFragment(new SettingsFragment());
                        break;
                    default:
                        bottomNavigationView.setSelectedItemId(R.id.home);
                        break;
                }
            } else {
                bottomNavigationView.setSelectedItemId(R.id.about);
                replaceFragment(new AboutFragment());
            }
        }

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.about) {
                replaceFragment(new AboutFragment());
            } else if (item.getItemId() == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Ẩn Fragment hiện tại nếu có
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        // Hiển thị hoặc thêm Fragment mới
        if (!newFragment.isAdded()) {
            fragmentTransaction.add(R.id.frame_layout, newFragment);
        } else {
            fragmentTransaction.show(newFragment);
        }

        // Cập nhật Fragment hiện tại
        currentFragment = newFragment;
        fragmentTransaction.commitAllowingStateLoss();
    }
}