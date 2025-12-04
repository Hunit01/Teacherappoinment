package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity {

    TextView tvTeacherName;
    RatingBar ratingBar, helpfulBar;
    EditText etComment;
    Button btnSubmitSurvey;
    ProgressBar progressBar;

    String appointmentId, teacherId, teacherName, studentId, studentName;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // Get data from Intent
        appointmentId = getIntent().getStringExtra("appointmentId");
        teacherId = getIntent().getStringExtra("teacherId");
        teacherName = getIntent().getStringExtra("teacherName");
        studentId = FirebaseUtils.uid();
        studentName = getIntent().getStringExtra("studentName");

        // Bind UI
        tvTeacherName = findViewById(R.id.tvSurveyTeacherName);
        ratingBar = findViewById(R.id.ratingBar);
        helpfulBar = findViewById(R.id.helpfulBar);
        etComment = findViewById(R.id.etSurveyComment);
        btnSubmitSurvey = findViewById(R.id.btnSubmitSurvey);
        progressBar = findViewById(R.id.progressSurvey);

        tvTeacherName.setText("Survey for: " + teacherName);

        btnSubmitSurvey.setOnClickListener(v -> submitSurvey());
    }

    private void submitSurvey() {

        float rating = ratingBar.getRating();
        float helpfulness = helpfulBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please give a rating!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmitSurvey.setEnabled(false);

        String surveyId = db.collection("surveys").document().getId();

        Map<String, Object> survey = new HashMap<>();
        survey.put("surveyId", surveyId);
        survey.put("teacherId", teacherId);
        survey.put("studentId", studentId);
        survey.put("studentName", studentName);
        survey.put("appointmentId", appointmentId);
        survey.put("rating", (int) rating);
        survey.put("helpfulness", (int) helpfulness);
        survey.put("comment", comment);
        survey.put("timestamp", System.currentTimeMillis());

        // Save survey
        db.collection("surveys")
                .document(surveyId)
                .set(survey)
                .addOnSuccessListener(unused -> {

                    // Update appointment flags
                    DocumentReference appointmentRef =
                            db.collection("appointments").document(appointmentId);

                    appointmentRef.update(
                            "surveyPending", false,
                            "surveyCompleted", true
                    );

                    Toast.makeText(this, "Survey submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitSurvey.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
