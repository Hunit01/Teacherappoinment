package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    Button btnViewStudents, btnViewTeachers, btnManageUsers, btnAddTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        String name = getIntent().getStringExtra("name");
        TextView tvWelcome = findViewById(R.id.tvWelcomeAdmin);
        tvWelcome.setText("Welcome, " + name);

        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnViewTeachers = findViewById(R.id.btnViewTeachers);
        btnManageUsers  = findViewById(R.id.btnManageUsers);
        btnAddTeacher   = findViewById(R.id.btnAddTeacher);

        btnViewStudents.setOnClickListener(v -> openList("Student"));
        btnViewTeachers.setOnClickListener(v -> openList("Teacher"));
        btnManageUsers.setOnClickListener(v -> openList("ALL"));

        btnAddTeacher.setOnClickListener(v -> showAddTeacherPopup());
    }

    private void openList(String filter) {
        Intent i = new Intent(this, UserListActivity.class);
        i.putExtra("filter", filter);
        startActivity(i);
    }

    private void showAddTeacherPopup() {

        View popup = LayoutInflater.from(this).inflate(R.layout.popup_add_teacher, null);

        EditText etName    = popup.findViewById(R.id.etTeacherName);
        EditText etSubject = popup.findViewById(R.id.etTeacherSubject);
        EditText etEmail   = popup.findViewById(R.id.etTeacherEmail);
        EditText etPass    = popup.findViewById(R.id.etTeacherPass);
        CheckBox cbRemember = popup.findViewById(R.id.cbRememberTeacher);

        new AlertDialog.Builder(this)
                .setTitle("Add New Teacher")
                .setView(popup)
                .setPositiveButton("Add", (dialog, which) -> {

                    String name    = etName.getText().toString().trim();
                    String subject = etSubject.getText().toString().trim();
                    String email   = etEmail.getText().toString().trim();
                    String pass    = etPass.getText().toString().trim();

                    if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || subject.isEmpty()) {
                        Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUtils.auth()
                            .createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(result -> {

                                String uid = result.getUser().getUid();

                                Map<String, Object> user = new HashMap<>();
                                user.put("uid", uid);
                                user.put("name", name);
                                user.put("email", email);
                                user.put("role", "Teacher");
                                user.put("subject", subject);
                                user.put("phone", "");
                                user.put("qualification", "");
                                user.put("experience", "");
                                user.put("description", "");

                                FirebaseUtils.db()
                                        .collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(unused -> {

                                            Toast.makeText(this, "Teacher Added!", Toast.LENGTH_SHORT).show();

                                            // ⭐ Remember teacher locally if checked
                                            if (cbRemember.isChecked()) {
                                                new Thread(() -> {
                                                    UserEntity u = new UserEntity(uid, name, email, pass, "Teacher");
                                                    AppDatabase.getInstance(this)
                                                            .userDao()
                                                            .saveUser(u);
                                                }).start();
                                            }
                                        });

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
