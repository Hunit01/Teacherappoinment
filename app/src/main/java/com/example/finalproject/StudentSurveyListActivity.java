package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class StudentSurveyListActivity extends AppCompatActivity {

    RecyclerView recycler;
    ProgressBar progressBar;
    TextView tvEmpty;
    Button btnPendingSurveys, btnCompletedSurveys;

    // Pending → appointments (Fill Survey)
    ArrayList<Appointment> pendingList = new ArrayList<>();
    AppointmentAdapter pendingAdapter;

    // Completed → surveys collection
    ArrayList<Survey> completedList = new ArrayList<>();
    SurveyAdapter completedAdapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_survey_list);

        studentId = FirebaseUtils.uid();

        recycler = findViewById(R.id.recyclerStudentSurveys);
        progressBar = findViewById(R.id.progressStudentSurveys);
        tvEmpty = findViewById(R.id.tvEmptyStudentSurveys);
        btnPendingSurveys = findViewById(R.id.btnPendingSurveys);
        btnCompletedSurveys = findViewById(R.id.btnCompletedSurveys);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        pendingAdapter = new AppointmentAdapter(this, pendingList);
        completedAdapter = new SurveyAdapter(this, completedList);

        // Default → pending
        showPending();

        btnPendingSurveys.setOnClickListener(v -> showPending());
        btnCompletedSurveys.setOnClickListener(v -> showCompleted());
    }

    private void showPending() {
        recycler.setAdapter(pendingAdapter);
        loadPendingSurveys();
    }

    private void showCompleted() {
        recycler.setAdapter(completedAdapter);
        loadCompletedSurveys();
    }

    // ⏳ Pending → appointments where surveyPending = true
    private void loadPendingSurveys() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("appointments")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("surveyPending", true)
                .whereEqualTo("surveyCompleted", false)
                .get()
                .addOnSuccessListener(snaps -> {
                    progressBar.setVisibility(View.GONE);
                    pendingList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Appointment a = doc.toObject(Appointment.class);
                        pendingList.add(a);
                    }

                    if (pendingList.isEmpty()) {
                        tvEmpty.setText("No pending surveys.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    pendingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setText("Error: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    // ✅ Completed → surveys collection
    private void loadCompletedSurveys() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("surveys")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(snaps -> {
                    progressBar.setVisibility(View.GONE);
                    completedList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Survey s = doc.toObject(Survey.class);
                        completedList.add(s);
                    }

                    if (completedList.isEmpty()) {
                        tvEmpty.setText("No completed surveys.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    completedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setText("Error: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}
