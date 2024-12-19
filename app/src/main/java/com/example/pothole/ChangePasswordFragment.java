package com.example.pothole;

import static android.webkit.URLUtil.isValidUrl;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {
    private ImageView imgShowHidePassword,imgShowHidePassword1;
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible1 = false;
    private EditText edtNewPassword, edtReNewPassword;
    private Button btnChangePassword;
    private SharedPreferences sharedPreferences;
    private ImageView imgAvatar;
    private TextView user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        edtNewPassword = view.findViewById(R.id.edt_newpassword);
        edtReNewPassword = view.findViewById(R.id.renewpassword);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        imgAvatar = view.findViewById(R.id.img_avatar);
        user = view.findViewById(R.id.user);
        loadUserInfo();
        imgShowHidePassword = view.findViewById(R.id.img_show_hide_password);
        edtReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);

        imgShowHidePassword1 = view.findViewById(R.id.img_show_hide_password1);
        edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword1.setImageResource(R.drawable.ic_eye_off);
        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);
        imgShowHidePassword1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible1) {
                    edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowHidePassword1.setImageResource(R.drawable.ic_eye);
                } else {
                    edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowHidePassword1.setImageResource(R.drawable.ic_eye_off);
                }
                edtNewPassword.setSelection(edtNewPassword.length());
                isPasswordVisible1 = !isPasswordVisible1;
            }
        });

        imgShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    edtReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowHidePassword.setImageResource(R.drawable.ic_eye);
                } else {
                    edtReNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);
                }
                edtReNewPassword.setSelection(edtReNewPassword.length());
                isPasswordVisible = !isPasswordVisible;
            }
        });
        btnChangePassword.setOnClickListener(v -> changePassword());

        return view;
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);

        String name = sharedPreferences.getString("username", "Guest");
        String email = sharedPreferences.getString("email", "No Email");
        String avatarString = sharedPreferences.getString("avatar", "");

        // Set dữ liệu vào TextView
        user.setText(name);

        if (!avatarString.isEmpty()) {
            if (isValidUrl(avatarString)) {
                // Nếu là URL, sử dụng Glide để tải ảnh
                Glide.with(this).load(avatarString).into(imgAvatar);
            } else {
                // Nếu không phải URL, giả định là Base64
                try {
                    if (avatarString.startsWith("data:image")) {
                        avatarString = avatarString.substring(avatarString.indexOf(",") + 1);
                    }
                    byte[] decodedString = android.util.Base64.decode(avatarString, android.util.Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgAvatar.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    imgAvatar.setImageResource(R.drawable.avatar_default); // Hiển thị ảnh mặc định nếu lỗi
                }
            }
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_default); // Hiển thị ảnh mặc định nếu không có avatar
        }
    }

    private void changePassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String reNewPassword = edtReNewPassword.getText().toString().trim();
        String email = sharedPreferences.getString("email", "No Email");

        if (newPassword.isEmpty() || reNewPassword.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(reNewPassword)) {
            Toast.makeText(getContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.changePassword(email, newPassword);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.password_updated_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.failed_to_update_password), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.connection_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
