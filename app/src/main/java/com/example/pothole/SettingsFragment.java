package com.example.pothole;

import static android.webkit.URLUtil.isValidUrl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView imgAvatar;
    private LinearLayout btn_edtprofile, changepassword, btn_selectlanguage, btn_logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        imgAvatar = view.findViewById(R.id.img_avatar);
        btn_edtprofile = view.findViewById(R.id.btn_edtprofile);
        changepassword = view.findViewById(R.id.changepassword);
        btn_selectlanguage = view.findViewById(R.id.btn_selectlanguage);
        btn_logout = view.findViewById(R.id.btn_logout);

        loadUserInfo();

        btn_logout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        requireActivity().finishAffinity();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        changepassword.setOnClickListener(view1 -> replaceFragment(new ChangePasswordFragment()));

        btn_edtprofile.setOnClickListener(view12 -> replaceFragment(new ProfileFragment()));

        btn_selectlanguage.setOnClickListener(v -> {
            final String[] languages = {"English", "Tiếng Việt", "Português"};
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Language")
                    .setItems(languages, (dialog, which) -> {
                        String selectedLanguage = which == 0 ? "en" : which == 1 ? "vi" : "pt";
                        setLocale(selectedLanguage);
                    })
                    .show();
        });

        return view;
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);

        String name = sharedPreferences.getString("username", "Guest");
        String email = sharedPreferences.getString("email", "No Email");
        String avatarString = sharedPreferences.getString("avatar", "");

        tvName.setText(name);
        tvEmail.setText(email);

        if (!avatarString.isEmpty()) {
            if (isValidUrl(avatarString)) {
                Glide.with(this).load(avatarString).into(imgAvatar);
            } else {
                try {
                    if (avatarString.startsWith("data:image")) {
                        avatarString = avatarString.substring(avatarString.indexOf(",") + 1);
                    }
                    byte[] decodedString = android.util.Base64.decode(avatarString, android.util.Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgAvatar.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    imgAvatar.setImageResource(R.drawable.avatar_default);
                }
            }
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_default);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setLocale(String languageCode) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppSettings", requireActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());
        requireActivity().recreate();
    }
}
