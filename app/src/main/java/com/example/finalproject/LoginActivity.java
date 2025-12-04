package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegisterLink;

    String selectedRole = "Student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        selectedRole = getIntent().getStringExtra("selectedRole");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ONLINE LOGIN
        FirebaseUtils.auth()
                .signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {

                    String uid = FirebaseUtils.uid();

                    FirebaseUtils.db().collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {

                                String role = doc.getString("role");
                                String name = doc.getString("name");
                                String profileUrl = doc.getString("profileImageUrl");

                                if (profileUrl == null || profileUrl.isEmpty()) {
                                    profileUrl = CloudinaryConfig.DEFAULT_AVATAR_URL;
                                }

                                // 🔥 Save offline copy ONLY FOR STUDENT
                                if ("Student".equals(role)) {
                                    String finalProfileUrl = profileUrl;
                                    new Thread(() -> {
                                        AppDatabase.getInstance(this)
                                                .userDao()
                                                .clearUser();

                                        UserEntity u = new UserEntity(uid, name, email, pass, "Student");
                                        u.profileImageUrl = finalProfileUrl;

                                        AppDatabase.getInstance(this)
                                                .userDao()
                                                .saveUser(u);
                                    }).start();
                                }

                                openDashboard(role, name);
                            });

                })
                .addOnFailureListener(e -> {

                    // OFFLINE LOGIN ONLY FOR STUDENT
                    new Thread(() -> {

                        UserEntity saved = AppDatabase.getInstance(this)
                                .userDao()
                                .getSavedUser();

                        runOnUiThread(() -> {

                            if (!"Student".equals(selectedRole)) {
                                Toast.makeText(this, "Offline login only for Student", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (saved == null) {
                                Toast.makeText(this, "No offline student saved", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (!saved.email.equals(email) || !saved.password.equals(pass)) {
                                Toast.makeText(this, "Invalid offline credentials", Toast.LENGTH_LONG).show();
                                return;
                            }

                            openDashboard("Student", saved.name);
                        });

                    }).start();
                });
    }

    private void openDashboard(String role, String name) {

        Intent intent;

        if (role.equals("Student")) {
            intent = new Intent(this, StudentDashboardActivity.class);
        } else if (role.equals("Teacher")) {
            intent = new Intent(this, TeacherDashboardActivity.class);
        } else if (role.equals("Admin")) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            return;
        }

        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }
}
