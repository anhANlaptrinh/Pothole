package com.example.pothole;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pothole.model.User;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;

    private EditText textEmailValue, textPasswordValue;
    private Button emailSignUpButton, googleSignUpButton, container_button_login;
    private ImageView imgShowHidePassword;
    private boolean isPasswordVisible = false;
    private TextView text_forgot_password;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
        setupGoogleSignIn();
        event();
    }

    public void setupUI() {
        String sign_in_google = getString(R.string.sign_in_google);
        String sign_up_email = getString(R.string.sign_up_email);
        googleSignUpButton = findViewById(R.id.container_another_step_google);
        Drawable googleIcon = getResources().getDrawable(R.drawable.image_google);
        googleIcon.setBounds(0, 0, googleIcon.getIntrinsicWidth(), googleIcon.getIntrinsicHeight());
        SpannableString spannableString = new SpannableString("  " + sign_in_google);
        ImageSpan imageSpan = new ImageSpan(googleIcon, ImageSpan.ALIGN_BOTTOM);
        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        googleSignUpButton.setText(spannableString);
        googleSignUpButton.setGravity(Gravity.CENTER);

        emailSignUpButton = findViewById(R.id.email_sign_up_button);
        Drawable logo = getResources().getDrawable(R.drawable.baseline_email_24);
        logo.setBounds(0, 0, logo.getIntrinsicWidth(), logo.getIntrinsicHeight());
        SpannableString spannableString1 = new SpannableString("  " + sign_up_email);
        ImageSpan imageSpan1 = new ImageSpan(logo, ImageSpan.ALIGN_BOTTOM);
        spannableString1.setSpan(imageSpan1, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        emailSignUpButton.setText(spannableString1);
        emailSignUpButton.setGravity(Gravity.CENTER);

        container_button_login = findViewById(R.id.container_button_login);
        textEmailValue = findViewById(R.id.text_email_value);
        textPasswordValue = findViewById(R.id.text_password_value);
        imgShowHidePassword = findViewById(R.id.img_show_hide_password);
        textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);
        text_forgot_password = findViewById(R.id.text_forgot_password);
    }

    private void setupGoogleSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void event() {
        container_button_login.setOnClickListener(v -> performLogin());

        googleSignUpButton.setOnClickListener(v -> signInWithGoogle());

        text_forgot_password.setOnClickListener(view -> {
            String email = textEmailValue.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            } else {
                ApiService apiService = ApiClient.getApiService();
                Call<Boolean> call = apiService.checkEmailExists(email);
                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful() && response.body() != null && response.body()) {
                            Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        imgShowHidePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgShowHidePassword.setImageResource(R.drawable.ic_eye);
            } else {
                textPasswordValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgShowHidePassword.setImageResource(R.drawable.ic_eye_off);
            }
            textPasswordValue.setSelection(textPasswordValue.length());
            isPasswordVisible = !isPasswordVisible;
        });

        emailSignUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken(); // Đây là token mà bạn cần
                String email = account.getEmail();
                Log.d("LoginActivity", "idToken: " + idToken);
                Log.d("LoginActivity", "Email: " + email);

                if (idToken != null) {
                    loginWithGoogle(idToken); // Dùng token để gọi API
                } else {
                    Toast.makeText(this, "Không lấy được idToken từ Google Sign-In.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ApiException e) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithGoogle(String idToken) {
        ApiService apiService = ApiClient.getApiService();
        Call<User> call = apiService.loginWithGoogle(idToken);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    saveLoginInfo(user);
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String email = textEmailValue.getText().toString().trim();
        String password = textPasswordValue.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = ApiClient.getApiService();
        Call<User> call = apiService.login(email, password);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressDialog.dismiss(); // Ẩn ProgressDialog khi có phản hồi

                if (response.isSuccessful()) {
                    User user = response.body();
                    saveLoginInfo(user);

                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss(); // Ẩn ProgressDialog khi có lỗi
                Toast.makeText(LoginActivity.this, getString(R.string.connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginInfo(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", user.getEmail());
        editor.putString("username", user.getUsername());
        editor.putString("avatar", user.getAvatar());
        editor.putString("accountType", user.getAccountTypes().toString());
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }
}
