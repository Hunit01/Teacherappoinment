package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    RecyclerView recyclerUsers;
    ArrayList<User> userList = new ArrayList<>();
    UserAdapter adapter;
    String filter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(userList);
        recyclerUsers.setAdapter(adapter);

        filter = getIntent().getStringExtra("filter");

        loadUsers();
    }

    private void loadUsers() {
        FirebaseUtils.db().collection("users")
                .get()
                .addOnSuccessListener(result -> {

                    userList.clear();

                    for (QueryDocumentSnapshot doc : result) {
                        User user = doc.toObject(User.class);
                        user.uid = doc.getId();

                        if (user.role.equals("Admin")) continue;

                        if (filter.equals("ALL") || user.role.equals(filter)) {
                            userList.add(user);
                        }
                    }

                    adapter.setMode(filter);
                    adapter.notifyDataSetChanged();
                });
    }
}
