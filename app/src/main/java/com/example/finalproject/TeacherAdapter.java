package com.example.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    ArrayList<User> list;
    Context context;

    public TeacherAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    // ⭐ REQUIRED for SearchFeature
    public void updateList(ArrayList<User> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        User t = list.get(pos);

        // collapsed info
        h.name.setText(t.name);
        h.subject.setText("Subject: " + (t.subject == null ? "N/A" : t.subject));

        // expanded info
        h.email.setText("Email: " + t.email);
        h.phone.setText("Phone: " + safe(t.phone));
        h.qualification.setText("Qualification: " + safe(t.qualification));
        h.experience.setText("Experience: " + safe(t.experience));
        h.description.setText("Description: " + safe(t.description));

        // expand/collapse
        h.itemView.setOnClickListener(v -> {
            if (h.layoutMore.getVisibility() == View.GONE)
                h.layoutMore.setVisibility(View.VISIBLE);
            else
                h.layoutMore.setVisibility(View.GONE);
        });

        // book appointment click
        h.book.setOnClickListener(v -> handleBooking(t));
    }

    private void handleBooking(User t) {

        String uid = FirebaseUtils.uid();

        // 1️⃣ If NOT logged in → ALWAYS show login popup
        if (uid == null || uid.trim().isEmpty()) {
            showLoginPopup();
            return;
        }

        // 2️⃣ If logged-in → check role from Firestore (ROOM NOT USED)
        FirebaseUtils.db().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        showLoginPopup();
                        return;
                    }

                    String role = doc.getString("role");

                    // ❌ Teacher or Admin CANNOT book
                    if (!"Student".equals(role)) {
                        new AlertDialog.Builder(context)
                                .setTitle("Not Allowed")
                                .setMessage("Only students can book appointments.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // 🎉 Student → Open booking page
                    Intent i = new Intent(context, BookAppointmentActivity.class);
                    i.putExtra("teacherId", t.uid);
                    i.putExtra("teacherName", t.name);
                    context.startActivity(i);

                })
                .addOnFailureListener(e -> showLoginPopup());
    }

    private void showLoginPopup() {
        new AlertDialog.Builder(context)
                .setTitle("Login Required")
                .setMessage("Please login to book an appointment.")
                .setPositiveButton("Login", (d, w) -> {
                    Intent i = new Intent(context, LoginActivity.class);
                    context.startActivity(i);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String safe(String s) {
        return s == null || s.isEmpty() ? "N/A" : s;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, subject;
        TextView email, phone, qualification, experience, description;
        LinearLayout layoutMore;
        Button book;

        public ViewHolder(@NonNull View v) {
            super(v);

            name = v.findViewById(R.id.tvName);
            subject = v.findViewById(R.id.tvSubject);

            email = v.findViewById(R.id.tvEmail);
            phone = v.findViewById(R.id.tvPhone);
            qualification = v.findViewById(R.id.tvQualification);
            experience = v.findViewById(R.id.tvExperience);
            description = v.findViewById(R.id.tvDescription);

            layoutMore = v.findViewById(R.id.layoutMore);
            book = v.findViewById(R.id.btnBook);
        }
    }
}
