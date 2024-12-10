package com.example.pothole.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("email")
    private String email;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("accountTypes") // Khai báo là List hoặc Set nếu server trả về dạng này
    private List<String> accountTypes;

    // Constructor không tham số
    public User() {}

    // Constructor với 5 tham số
    public User(String username, String password, String email, String avatar, List<String> accountTypes) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
        this.accountTypes = accountTypes;
    }

    // Constructor với 3 tham số (dành cho Android)
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = null;
        this.accountTypes = List.of("email"); // Mặc định cho Android
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<String> getAccountTypes() { return accountTypes; }
    public void setAccountTypes(List<String> accountTypes) { this.accountTypes = accountTypes; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", accountTypes='" + accountTypes + '\'' +
                '}';
    }
}
