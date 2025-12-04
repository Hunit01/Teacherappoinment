package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllAppointmentsActivity extends AppCompatActivity {

    RecyclerView recycler;
    AppointmentAdapter adapter;
    ArrayList<Appointment> list = new ArrayList<>();

    Button btnPending, btnApproved, btnDeclined;
    Button btnFilterApproved, btnFilterCompleted;
    Button btnFilterDeclined, btnFilterCancelled;

    LinearLayout layoutApprovedOptions, layoutDeclinedOptions;

    String teacherId;

    AppDatabase localDb;
    AppointmentDao appointmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_appointments);

        recycler = findViewById(R.id.recyclerAppointments);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(this, list);
        recycler.setAdapter(adapter);

        btnPending = findViewById(R.id.btnPending);
        btnApproved = findViewById(R.id.btnApproved);
        btnDeclined = findViewById(R.id.btnDeclined);

        layoutApprovedOptions = findViewById(R.id.layoutApprovedOptions);
        layoutDeclinedOptions = findViewById(R.id.layoutDeclinedOptions);

        btnFilterApproved = findViewById(R.id.btnFilterApproved);
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        btnFilterDeclined = findViewById(R.id.btnFilterDeclined);
        btnFilterCancelled = findViewById(R.id.btnFilterCancelled);

        localDb = AppDatabase.getInstance(getApplicationContext());
        appointmentDao = localDb.appointmentDao();

        initTeacherAndSetListeners();
    }

    private void initTeacherAndSetListeners() {

        // ⭐ FIX: ALWAYS USE FIREBASE AUTH UID FOR TEACHER
        teacherId = FirebaseUtils.uid();

        runOnUiThread(() -> {

            if (teacherId == null) {
                Toast.makeText(this, "No teacher id found!", Toast.LENGTH_LONG).show();
                return;
            }

            loadPendingFromServer(); // Default

            btnPending.setOnClickListener(v -> {
                layoutApprovedOptions.setVisibility(View.GONE);
                layoutDeclinedOptions.setVisibility(View.GONE);
                loadPendingFromServer();
            });

            btnApproved.setOnClickListener(v -> {
                if (layoutApprovedOptions.getVisibility() == View.VISIBLE)
                    layoutApprovedOptions.setVisibility(View.GONE);
                else {
                    layoutApprovedOptions.setVisibility(View.VISIBLE);
                    layoutDeclinedOptions.setVisibility(View.GONE);
                }
            });

            btnFilterApproved.setOnClickListener(v -> loadApprovedFromServer(false));
            btnFilterCompleted.setOnClickListener(v -> loadApprovedFromServer(true));

            btnDeclined.setOnClickListener(v -> {
                if (layoutDeclinedOptions.getVisibility() == View.VISIBLE)
                    layoutDeclinedOptions.setVisibility(View.GONE);
                else {
                    layoutDeclinedOptions.setVisibility(View.VISIBLE);
                    layoutApprovedOptions.setVisibility(View.GONE);
                }
            });

            btnFilterDeclined.setOnClickListener(v -> loadByStatusFromServer("Declined"));
            btnFilterCancelled.setOnClickListener(v -> loadByStatusFromServer("Cancelled"));
        });
    }


    private void loadPendingFromServer() {

        FirebaseUtils.db().collection("appointments")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(result -> {

                    ArrayList<Appointment> fresh = new ArrayList<>();

                    for (QueryDocumentSnapshot d : result) {
                        fresh.add(d.toObject(Appointment.class));
                    }

                    updateListAndSaveLocal(fresh);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Offline mode (Pending)", Toast.LENGTH_SHORT).show();
                    loadPendingFromLocal();
                });
    }

    private void loadApprovedFromServer(boolean completedOnly) {

        FirebaseUtils.db().collection("appointments")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(result -> {

                    ArrayList<Appointment> fresh = new ArrayList<>();

                    for (QueryDocumentSnapshot d : result) {

                        Appointment a = d.toObject(Appointment.class);

                        if (!completedOnly) {
                            // Approved but NOT completed
                            if ("Approved".equals(a.status) && !a.completed)
                                fresh.add(a);
                        } else {
                            // Completed appointments
                            if (a.completed)
                                fresh.add(a);
                        }
                    }

                    updateListAndSaveLocal(fresh);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Offline mode (Approved)", Toast.LENGTH_SHORT).show();

                    if (completedOnly) loadCompletedFromLocal();
                    else loadApprovedNotCompletedFromLocal();
                });
    }

    private void loadByStatusFromServer(String status) {

        FirebaseUtils.db().collection("appointments")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(result -> {

                    ArrayList<Appointment> fresh = new ArrayList<>();

                    for (QueryDocumentSnapshot d : result) {
                        fresh.add(d.toObject(Appointment.class));
                    }

                    updateListAndSaveLocal(fresh);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Offline mode", Toast.LENGTH_SHORT).show();
                    loadByStatusFromLocal(status);
                });
    }

    private void updateListAndSaveLocal(ArrayList<Appointment> fresh) {
        list.clear();
        list.addAll(fresh);
        adapter.notifyDataSetChanged();

        new Thread(() -> appointmentDao.insertAll(fresh)).start();
    }

    // -------- LOCAL DB LOADERS -------
    private void loadPendingFromLocal() {
        new Thread(() -> {
            List<Appointment> local = appointmentDao.getAppointmentsForTeacherByStatus(teacherId, "Pending");
            runOnUiThread(() -> {
                list.clear();
                list.addAll(local);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void loadApprovedNotCompletedFromLocal() {
        new Thread(() -> {
            List<Appointment> local = appointmentDao.getTeacherApprovedNotCompleted(teacherId);
            runOnUiThread(() -> {
                list.clear();
                list.addAll(local);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void loadCompletedFromLocal() {
        new Thread(() -> {
            List<Appointment> local = appointmentDao.getTeacherCompleted(teacherId);
            runOnUiThread(() -> {
                list.clear();
                list.addAll(local);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void loadByStatusFromLocal(String status) {
        new Thread(() -> {
            List<Appointment> local = appointmentDao.getAppointmentsForTeacherByStatus(teacherId, status);
            runOnUiThread(() -> {
                list.clear();
                list.addAll(local);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
