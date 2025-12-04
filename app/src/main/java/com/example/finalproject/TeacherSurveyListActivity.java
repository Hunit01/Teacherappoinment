package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TeacherSurveyListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView tvEmpty;

    ArrayList<Survey> surveyList = new ArrayList<>();
    TeacherSurveyAdapter adapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_survey_list);

        teacherId = FirebaseUtils.uid();

        recyclerView = findViewById(R.id.recyclerViewSurveys);
        progressBar = findViewById(R.id.progressSurveys);
        tvEmpty = findViewById(R.id.tvEmptyMessage);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeacherSurveyAdapter(this, surveyList);
        recyclerView.setAdapter(adapter);

        loadSurveys();
    }

    private void loadSurveys() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("surveys")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snaps -> {
                    progressBar.setVisibility(View.GONE);

                    surveyList.clear();

                    for (QueryDocumentSnapshot doc : snaps) {
                        Survey s = doc.toObject(Survey.class);
                        surveyList.add(s);
                    }

                    if (surveyList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setText("Error loading surveys: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}
