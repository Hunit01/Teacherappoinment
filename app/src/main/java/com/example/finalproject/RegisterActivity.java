package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Button btnRegister;
    CheckBox checkRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        checkRemember = findViewById(R.id.checkRemember);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUtils.auth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("role", "Student");
                    // 🔹 Default profile image in Firestore
                    user.put("profileImageUrl", CloudinaryConfig.DEFAULT_AVATAR_URL);

                    FirebaseUtils.db().collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                // ⭐ Save in Room only IF Remember Me checked
                                if (checkRemember.isChecked()) {
                                    new Thread(() -> {
                                        UserEntity u = new UserEntity(uid, name, email, password, "Student");
                                        u.profileImageUrl = CloudinaryConfig.DEFAULT_AVATAR_URL;
                                        AppDatabase.getInstance(this)
                                                .userDao()
                                                .saveUser(u);
                                    }).start();
                                }

                                Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
