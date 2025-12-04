package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class SearchTeacherActivity extends AppCompatActivity {

    EditText etSearch;
    RecyclerView recyclerTeachers;

    ArrayList<User> list = new ArrayList<>();
    TeacherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_teacher);

        etSearch = findViewById(R.id.etSearchTeacher);
        recyclerTeachers = findViewById(R.id.recyclerTeachers);

        // 🔥 Use new TeacherAdapter constructor
        adapter = new TeacherAdapter(list, this);

        recyclerTeachers.setLayoutManager(new LinearLayoutManager(this));
        recyclerTeachers.setAdapter(adapter);

        loadTeachers();

        // 🔍 Live search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTeachers() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("role", "Teacher")
                .get()
                .addOnSuccessListener(snaps -> {
                    list.clear();
                    for (QueryDocumentSnapshot doc : snaps) {
                        User u = doc.toObject(User.class);
                        u.uid = doc.getId();
                        list.add(u);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filter(String text) {
        ArrayList<User> temp = new ArrayList<>();
        for (User u : list) {
            if (u.name != null && u.name.toLowerCase().contains(text.toLowerCase())) {
                temp.add(u);
            }
        }

        // 🔥 Use updateList safely
        adapter.list = temp;
        adapter.notifyDataSetChanged();
    }
}
