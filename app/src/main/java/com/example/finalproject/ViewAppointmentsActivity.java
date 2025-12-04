package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAppointmentsActivity extends AppCompatActivity {

    RecyclerView recycler;
    AppointmentAdapter adapter;
    ArrayList<Appointment> list = new ArrayList<>();

    AppDatabase localDb;
    AppointmentDao appointmentDao;

    String uid;   // studentId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        recycler = findViewById(R.id.recyclerAppointments);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(this, list);
        recycler.setAdapter(adapter);

        localDb = AppDatabase.getInstance(getApplicationContext());
        appointmentDao = localDb.appointmentDao();

        resolveUserAndLoad();
    }

    private void resolveUserAndLoad() {

        new Thread(() -> {

            UserEntity saved = localDb.userDao().getSavedUser();

            if (saved != null) {
                uid = saved.uid;   // ⭐ offline UID
            } else {
                uid = FirebaseUtils.uid(); // online
            }

            runOnUiThread(() -> {

                if (uid == null) {
                    Toast.makeText(this, "No user found", Toast.LENGTH_LONG).show();
                    return;
                }

                loadAppointments();
            });

        }).start();
    }

    private void loadAppointments() {

        // --------------------------------------
        // If offline → directly load from ROOM
        // --------------------------------------
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "Offline mode", Toast.LENGTH_SHORT).show();
            loadLocalAppointments();
            return;
        }

        // --------------------------------------
        // ONLINE MODE → Firestore
        // --------------------------------------
        FirebaseFirestore.getInstance()
                .collection("appointments")
                .whereEqualTo("studentId", uid)
                .get()
                .addOnSuccessListener(snap -> {

                    ArrayList<Appointment> fresh = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        Appointment a = doc.toObject(Appointment.class);
                        fresh.add(a);
                    }

                    // UI update
                    list.clear();
                    list.addAll(fresh);
                    adapter.notifyDataSetChanged();

                    // ⭐ Save fresh list locally for offline
                    new Thread(() -> {
                        appointmentDao.insertAll(fresh);
                    }).start();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Offline mode", Toast.LENGTH_SHORT).show();
                    loadLocalAppointments();
                });
    }

    private void loadLocalAppointments() {

        new Thread(() -> {

            List<Appointment> localList =
                    appointmentDao.getAppointmentsForStudent(uid);

            runOnUiThread(() -> {
                list.clear();
                list.addAll(localList);
                adapter.notifyDataSetChanged();
            });

        }).start();
    }
}
