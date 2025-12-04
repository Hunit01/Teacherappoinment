package com.example.finalproject;

public class User {
    public String uid;
    public String name;
    public String email;
    public String role;

    // 🔹 extra profile fields (optional for student/admin, mainly for teachers)
    public String subject;
    public String phone;
    public String qualification;
    public String experience;
    public String description;

    public User() {
        // required empty constructor for Firestore
    }

    public User(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // (optional) full constructor if you ever want it
    public User(String uid, String name, String email, String role,
                String subject, String phone, String qualification,
                String experience, String description) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.subject = subject;
        this.phone = phone;
        this.qualification = qualification;
        this.experience = experience;
        this.description = description;
    }
}
