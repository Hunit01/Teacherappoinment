package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RescheduleActivity extends AppCompatActivity {

    TextView tvTeacherName, tvDate, tvTime;
    EditText etMessage;
    Button btnPickDate, btnPickTime, btnReschedule;

    String appointmentId, teacherName, originalMessage, oldDate, oldTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);

        // 🔹 Get values passed from AppointmentAdapter
        appointmentId   = getIntent().getStringExtra("appointmentId");
        teacherName     = getIntent().getStringExtra("teacherName");
        originalMessage = getIntent().getStringExtra("message");
        oldDate         = getIntent().getStringExtra("date");
        oldTime         = getIntent().getStringExtra("time");

        // 🔹 Bind UI
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvDate        = findViewById(R.id.tvDate);
        tvTime        = findViewById(R.id.tvTime);
        etMessage     = findViewById(R.id.etMessage);
        btnPickDate   = findViewById(R.id.btnPickDate);
        btnPickTime   = findViewById(R.id.btnPickTime);
        btnReschedule = findViewById(R.id.btnReschedule);

        // 🔹 Prefill UI
        tvTeacherName.setText("Teacher: " + teacherName);
        tvDate.setText(oldDate != null ? oldDate : "Select Date");
        tvTime.setText(oldTime != null ? oldTime : "Select Time");

        if (originalMessage != null)
            etMessage.setText(originalMessage);

        btnPickDate.setOnClickListener(v -> pickDate());
        btnPickTime.setOnClickListener(v -> pickTime());
        btnReschedule.setOnClickListener(v -> saveRescheduled());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (dp, y, m, d) -> tvDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (tp, h, min) -> tvTime.setText(String.format("%02d:%02d", h, min)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveRescheduled() {
        String newDate = tvDate.getText().toString().trim();
        String newTime = tvTime.getText().toString().trim();
        String newMsg  = etMessage.getText().toString().trim();

        if (newDate.equals("Select Date") || newTime.equals("Select Time")) {
            Toast.makeText(this, "Please choose date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newMsg.isEmpty()) newMsg = originalMessage;

        Map<String, Object> map = new HashMap<>();
        map.put("date", newDate);
        map.put("timeSlot", newTime);
        map.put("message", newMsg);
        map.put("status", "Pending");     // back to teacher pending
        map.put("rescheduled", true);     // teacher sees “Rescheduled”

        FirebaseFirestore.getInstance()
                .collection("appointments")
                .document(appointmentId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Reschedule request sent", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
