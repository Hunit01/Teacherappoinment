package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BookAppointmentActivity extends AppCompatActivity {

    TextView tvTeacherName, tvDate, tvTime;
    EditText etMessage;
    Button btnPickDate, btnPickTime, btnBook;

    String teacherId, teacherName;
    String studentId, studentName;

    UserEntity localUser;   // OFFLINE USER

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        teacherId = getIntent().getStringExtra("teacherId");
        teacherName = getIntent().getStringExtra("teacherName");

        studentId = FirebaseUtils.uid();

        // -----------------------------------------
        // GET STUDENT NAME (online OR offline)
        // -----------------------------------------
        fetchStudentName();

        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        etMessage = findViewById(R.id.etMessage);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnBook = findViewById(R.id.btnBook);

        tvTeacherName.setText("Teacher: " + teacherName);

        btnPickDate.setOnClickListener(v -> pickDate());
        btnPickTime.setOnClickListener(v -> pickTime());
        btnBook.setOnClickListener(v -> saveAppointment());
    }

    // ------------------------------------------------
    // FIX 1: Get real student name (online/offline)
    // ------------------------------------------------
    private void fetchStudentName() {

        new Thread(() -> {
            localUser = AppDatabase.getInstance(this).userDao().getSavedUser();

            if (localUser != null && localUser.uid.equals(studentId)) {
                studentName = localUser.name;
            } else {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(studentId)
                        .get()
                        .addOnSuccessListener(doc -> studentName = doc.getString("name"));
            }
        }).start();
    }


    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (dp, y, m, d) ->
                tvDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (tp, h, m) ->
                tvTime.setText(h + ":" + String.format("%02d", m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE), true)
                .show();
    }

    private void saveAppointment() {

        if (tvDate.getText().toString().equals("Select Date") ||
                tvTime.getText().toString().equals("Select Time")) {

            Toast.makeText(this, "Pick date & time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentName == null || studentName.isEmpty()) {
            Toast.makeText(this, "Error: Student name not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMessage.getText().toString();

        String apptId = FirebaseFirestore.getInstance()
                .collection("appointments")
                .document()
                .getId();

        Map<String, Object> appt = new HashMap<>();
        appt.put("appointmentId", apptId);
        appt.put("teacherId", teacherId);
        appt.put("teacherName", teacherName);
        appt.put("studentId", studentId);
        appt.put("studentName", studentName);   // FIXED ✔
        appt.put("message", message);
        appt.put("date", tvDate.getText().toString());
        appt.put("timeSlot", tvTime.getText().toString());
        appt.put("status", "Pending");
        appt.put("createdAt", System.currentTimeMillis());
        appt.put("rescheduled", false);

        FirebaseFirestore.getInstance()
                .collection("appointments")
                .document(apptId)
                .set(appt)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Appointment Booked!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
