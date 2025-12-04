package com.example.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<User> teacherList = new ArrayList<>();
    TeacherAdapter adapter;

    Button btnStudent, btnTeacher, btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler = findViewById(R.id.recyclerTeachersGuest);
        btnStudent = findViewById(R.id.btnStudent);
        btnTeacher = findViewById(R.id.btnTeacher);
        btnAdmin   = findViewById(R.id.btnAdmin);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherAdapter(teacherList, this);
        recycler.setAdapter(adapter);

        // role-specific login buttons
        btnStudent.setOnClickListener(v -> openLogin("Student"));
        btnTeacher.setOnClickListener(v -> openLogin("Teacher"));
        btnAdmin.setOnClickListener(v -> openLogin("Admin"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🔥 every time you come back to main screen, reload teachers
        loadTeachers();
    }

    private void openLogin(String role) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("selectedRole", role);
        startActivity(intent);
    }

    private void loadTeachers() {
        teacherList.clear();  // important so old data is removed

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "Teacher")
                .get()
                .addOnSuccessListener(result -> {
                    for (QueryDocumentSnapshot doc : result) {
                        User u = doc.toObject(User.class);
                        // make sure uid is filled (in case field missing)
                        if (u.uid == null || u.uid.isEmpty()) {
                            u.uid = doc.getId();
                        }
                        teacherList.add(u);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
