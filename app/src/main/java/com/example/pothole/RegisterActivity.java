package com.example.pothole;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pothole.model.User;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ImageView imgShowHidePassword,imgShowHidePassword1;
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible1 = false;
    private Button googleSignUpButton, create_account_button;
    private ImageButton button_back;
    private EditText textPasswordValue, textRepasswordValue, textUsernameValue, textEmailValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupUI();
        event();
    }

    public void setupUI() {
        create_account_button = findViewById(R.id.create_account_button);
        button_back = findViewById(R.id.button_back);
        textPasswordValue = findViewById(R.id.text_password_value);
        textRepasswordValue = findViewById(R.id.text_repassword_value);
        textUsernameValue = findViewById(R.id.username_input);
        textEmailValue = findViewById(R.id.email_input);

        imgShowHidePassword = findViewById(R.id.img_show_hide_password);
        textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);

        imgShowHidePassword1 = findViewById(R.id.img_show_hide_password1);
        textRepasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword1.setImageResource(R.drawable.ic_eye_off);
    }

    private void event() {
        create_account_button.setOnClickListener(v -> performRegister());

        imgShowHidePassword1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible1) {
                    textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowHidePassword1.setImageResource(R.drawable.ic_eye);
                } else {
                    textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowHidePassword1.setImageResource(R.drawable.ic_eye_off);
                }
                textPasswordValue.setSelection(textPasswordValue.length());
                isPasswordVisible1 = !isPasswordVisible1;
            }
        });

        imgShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    textRepasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgShowHidePassword.setImageResource(R.drawable.ic_eye);
                } else {
                    textRepasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);
                }
                textRepasswordValue.setSelection(textRepasswordValue.length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performRegister() {
        String username = textUsernameValue.getText().toString().trim();
        String password = textPasswordValue.getText().toString().trim();
        String email = textEmailValue.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        User newUser = new User(username, password, email);

        Call<User> call = apiService.register(newUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển đến màn hình đăng nhập hoặc màn hình chính sau khi đăng ký
                } else {
                    try {
                        // Lấy thông báo lỗi từ phản hồi của server
                        String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Đăng ký thất bại";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}