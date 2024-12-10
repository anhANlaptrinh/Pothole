package com.example.pothole.network;

import com.example.pothole.model.Pothole;
import com.example.pothole.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // Endpoint cho login
    @POST("api/auth/login")
    Call<User> login(@Query("email") String email, @Query("password") String password);

    // Endpoint cho register
    @POST("api/auth/register")
    Call<User> register(@Body User user);

    // Endpoint cho checkEmail
    @POST("api/auth/checkEmail")
    Call<Boolean> checkEmailExists(@Query("email") String email);

    // Endpoint cho getUserByEmail
    @GET("api/auth/getUserByEmail")
    Call<User> getUserByEmail(@Query("email") String email);

    // Endpoint cho changePassword
    @PUT("api/auth/changePassword")
    Call<Void> changePassword(
            @Query("email") String email,
            @Query("newPassword") String newPassword
    );

    @POST("/api/auth/loginWithGoogle")
    Call<User> loginWithGoogle(@Query("idToken") String idToken);

    // Endpoint cho forgotPassword
    @POST("api/auth/forgotPassword")
    Call<Void> forgotPassword(@Query("email") String email);

    // Endpoint cho verifyCode
    @POST("api/auth/verifyCode")
    Call<Void> verifyCode(@Query("email") String email, @Query("code") String code);

    // Endpoint cho resetPassword
    @PUT("api/auth/resetPassword")
    Call<Void> resetPassword(@Query("email") String email, @Query("newPassword") String newPassword);

    @PUT("api/auth/updateProfile")
    Call<Void> updateProfile(@Body Map<String, String> request);

    @GET("/api/pothole/list")
    Call<List<Pothole>> getPotholes();

    @POST("api/pothole/add")
    Call<Pothole> addPothole(@Body Pothole pothole);
}
