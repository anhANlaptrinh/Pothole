package com.example.pothole;

import static android.webkit.URLUtil.isValidUrl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.pothole.network.ApiClient;
import com.example.pothole.network.ApiService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Fragment currentFragment;
    private ImageView imgAvatar;
    private EditText edtFullName;
    private TextView tvEmail, fullname1;
    private Button btnUpdateProfile;
    private SharedPreferences sharedPreferences;

    private Uri selectedImageUri;
    private String base64Image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.img_avatar);
        edtFullName = view.findViewById(R.id.edt_full_name);
        tvEmail = view.findViewById(R.id.tv_email);
        btnUpdateProfile = view.findViewById(R.id.btn_update_profile);
        fullname1 = view.findViewById(R.id.user);
        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", requireActivity().MODE_PRIVATE);

        loadUserInfo();
        ImageButton btnBack = view.findViewById(R.id.button_back);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        imgAvatar.setOnClickListener(v -> openGallery());
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        return view;
    }

    private void loadUserInfo() {
        String avatarString = sharedPreferences.getString("avatar", "");
        String fullName = sharedPreferences.getString("username", "Guest");
        String email = sharedPreferences.getString("email", "No Email");

        edtFullName.setText(fullName);
        tvEmail.setText(email);
        fullname1.setText(fullName);

        if (!avatarString.isEmpty()) {
            if (isValidUrl(avatarString)) {
                Glide.with(this).load(avatarString).into(imgAvatar);
            } else {
                try {
                    byte[] decodedString = Base64.decode(avatarString, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgAvatar.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    imgAvatar.setImageResource(R.drawable.ic_avatar_default);
                }
            }
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_default);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgAvatar.setImageURI(selectedImageUri);
            processImageToBase64(selectedImageUri);
        }
    }

    private void processImageToBase64(Uri imageUri) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        String newName = edtFullName.getText().toString().trim();
        String avatarString = sharedPreferences.getString("avatar", "");
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (base64Image != null) {
            updateProfileOnServer(newName, base64Image);
        } else {
            updateProfileOnServer(newName, avatarString);
        }
    }

    private void updateProfileOnServer(String username, String base64Image) {
        String email = sharedPreferences.getString("email", "No Email");
        String fullName = sharedPreferences.getString("username", "Guest");

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("username", username);
        request.put("avatar", base64Image);

        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.updateProfile(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString("username", username).apply();
                    sharedPreferences.edit().putString("avatar", base64Image).apply();
                    fullname1.setText(fullName);
                } else {
                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
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
        fragmentTransaction.commitAllowingStateLoss(); // Dùng commitAllowingStateLoss để xử lý an toàn
    }
}
