package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PendingRequestsActivity extends AppCompatActivity {

    RecyclerView recycler;
    AppointmentAdapter adapter;
    ArrayList<Appointment> list = new ArrayList<>();
    String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        teacherId = FirebaseUtils.uid();

        recycler = findViewById(R.id.recyclerPending);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // FIXED
        adapter = new AppointmentAdapter(this, list);
        recycler.setAdapter(adapter);

        load();
    }

    private void load() {

        FirebaseUtils.db().collection("appointments")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(result -> {

                    list.clear();

                    for (QueryDocumentSnapshot d : result) {
                        Appointment a = d.toObject(Appointment.class);
                        list.add(a);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
