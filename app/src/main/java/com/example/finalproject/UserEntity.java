package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String uid;

    public String name;
    public String email;
    public String password;
    public String role;

    // 🔹 Profile photo URL
    public String profileImageUrl;

    // 🔹 NEW FIELDS REQUIRED BY StudentDashboardActivity
    public String phone;          // optional
    public String studentClass;   // optional

    // ⭐ Room needs ONLY ONE good constructor
    public UserEntity(@NonNull String uid,
                      String name,
                      String email,
                      String password,
                      String role) {

        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;

        // Default values
        this.profileImageUrl = CloudinaryConfig.DEFAULT_AVATAR_URL;
        this.phone = "";
        this.studentClass = "";
    }

    // ⭐ IMPORTANT:
    // Room requires an empty constructor IF using multiple fields
    public UserEntity() {
        this.uid = "";
        this.name = "";
        this.email = "";
        this.password = "";
        this.role = "";
        this.profileImageUrl = CloudinaryConfig.DEFAULT_AVATAR_URL;
        this.phone = "";
        this.studentClass = "";
    }
}
