package com.example.pothole;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotActivity extends AppCompatActivity {
    private EditText code_input, text_password_value, text_repassword_value, email_input;
    private Button send_code, reset_password;
    private ImageButton button_back;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupUI();
        getDataEmail();
        event();
    }

    private void getDataEmail() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email")) {
            String email = intent.getStringExtra("email");
            email_input.setText(email);
            email_input.setEnabled(false);
        }
    }
    private void setupUI() {
        code_input = findViewById(R.id.code_input);
        text_password_value = findViewById(R.id.text_password_value);
        text_repassword_value = findViewById(R.id.text_repassword_value);
        email_input = findViewById(R.id.email_input);
        send_code = findViewById(R.id.send_code);
        reset_password = findViewById(R.id.reset_password);
        button_back = findViewById(R.id.button_back);
    }

    private void event() {
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        send_code.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(ForgotActivity.this);
            progressDialog.setMessage("Đang kiểm tra email và gửi mã...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            String email = email_input.getText().toString().trim();
            if (email.isEmpty()) {
                progressDialog.dismiss();
                Toast.makeText(ForgotActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = ApiClient.getApiService();
            Call<Boolean> callCheckEmail = apiService.checkEmailExists(email);

            callCheckEmail.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean emailExists = response.body();
                        if (emailExists) {
                            Call<Void> callForgotPassword = apiService.forgotPassword(email);
                            callForgotPassword.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    progressDialog.dismiss();
                                    if (response.isSuccessful()) {
                                        Toast.makeText(ForgotActivity.this, "Đã gửi mã xác thực đến email của bạn", Toast.LENGTH_SHORT).show();
                                        startCountDownTimer();
                                    } else {
                                        Toast.makeText(ForgotActivity.this, "Không thể gửi mã xác thực", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ForgotActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ForgotActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotActivity.this, "Lỗi khi kiểm tra email", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        reset_password.setOnClickListener(view -> {
            String code = code_input.getText().toString().trim();
            String password = text_password_value.getText().toString().trim();
            String rePassword = text_repassword_value.getText().toString().trim();
            String email = email_input.getText().toString().trim();

            // Kiểm tra nếu các trường không rỗng
            if (code.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(ForgotActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu và nhập lại mật khẩu giống nhau
            if (!password.equals(rePassword)) {
                Toast.makeText(ForgotActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị ProgressDialog trong khi kiểm tra mã
            ProgressDialog progressDialog = new ProgressDialog(ForgotActivity.this);
            progressDialog.setMessage("Đang kiểm tra mã và đặt lại mật khẩu...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiService apiService = ApiClient.getApiService();

            // Gọi API verifyCode để kiểm tra mã xác thực
            Call<Void> callVerifyCode = apiService.verifyCode(email, code);
            callVerifyCode.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Mã xác thực đúng, tiến hành reset password
                        Call<Void> callResetPassword = apiService.resetPassword(email, password);
                        callResetPassword.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                progressDialog.dismiss();
                                if (response.isSuccessful()) {
                                    Toast.makeText(ForgotActivity.this, "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ForgotActivity.this, "Không thể đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                progressDialog.dismiss();
                                Toast.makeText(ForgotActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotActivity.this, "Mã xác thực không đúng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void startCountDownTimer() {
        send_code.setEnabled(false);
        send_code.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ForgotActivity.this, R.color.gray))); // Đổi màu sang màu xám

        countDownTimer = new CountDownTimer(600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                send_code.setText("Gửi lại sau " + time); // Hiển thị thời gian đếm ngược
            }

            public void onFinish() {
                send_code.setEnabled(true);
                send_code.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ForgotActivity.this, R.color.original_button_color)));
                send_code.setText("Gửi mã");
            }
        }.start();
    }
}