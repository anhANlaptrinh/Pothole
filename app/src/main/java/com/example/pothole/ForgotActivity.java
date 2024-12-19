package com.example.pothole;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private boolean isPasswordVisible = false;
    private boolean isPasswordVisible1 = false;
    private ImageView imgShowHidePassword,imgShowHidePassword1;
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
        imgShowHidePassword = findViewById(R.id.img_show_hide_password);
        text_repassword_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);

        imgShowHidePassword1 = findViewById(R.id.img_show_hide_password1);
        text_password_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword1.setImageResource(R.drawable.ic_eye_off);
    }

    private void event() {
        button_back.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        send_code.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(ForgotActivity.this);
            progressDialog.setMessage(getString(R.string.sending_verification_code)); // Đa ngôn ngữ
            progressDialog.setCancelable(false);
            progressDialog.show();

            String email = email_input.getText().toString().trim();
            if (email.isEmpty()) {
                progressDialog.dismiss();
                Toast.makeText(ForgotActivity.this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(ForgotActivity.this, getString(R.string.code_sent_success), Toast.LENGTH_SHORT).show();
                                        startCountDownTimer();
                                    } else {
                                        Toast.makeText(ForgotActivity.this, getString(R.string.cannot_send_code), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ForgotActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ForgotActivity.this, getString(R.string.email_not_exist), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotActivity.this, getString(R.string.email_check_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        });

        reset_password.setOnClickListener(view -> {
            String code = code_input.getText().toString().trim();
            String password = text_password_value.getText().toString().trim();
            String rePassword = text_repassword_value.getText().toString().trim();

            if (code.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(ForgotActivity.this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(rePassword)) {
                Toast.makeText(ForgotActivity.this, getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(ForgotActivity.this);
            progressDialog.setMessage(getString(R.string.resetting_password));
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiService apiService = ApiClient.getApiService();
            Call<Void> callVerifyCode = apiService.verifyCode(email_input.getText().toString().trim(), code);

            callVerifyCode.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Call<Void> callResetPassword = apiService.resetPassword(email_input.getText().toString().trim(), password);
                        callResetPassword.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                progressDialog.dismiss();
                                if (response.isSuccessful()) {
                                    Toast.makeText(ForgotActivity.this, getString(R.string.reset_password_success), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(ForgotActivity.this, getString(R.string.cannot_reset_password), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                progressDialog.dismiss();
                                Toast.makeText(ForgotActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotActivity.this, getString(R.string.invalid_code), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                send_code.setText(getString(R.string.resend_after) + " " + time);
            }

            @Override
            public void onFinish() {
                send_code.setEnabled(true);
                send_code.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ForgotActivity.this, R.color.original_button_color)));
                send_code.setText(getString(R.string.send_code1));
            }
        }.start();
    }
}