package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TeacherSurveyAdapter extends RecyclerView.Adapter<TeacherSurveyAdapter.ViewHolder> {

    Context context;
    ArrayList<Survey> list;

    public TeacherSurveyAdapter(Context context, ArrayList<Survey> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_survey_teacher, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Survey s = list.get(position);

        h.student.setText("Student: " + s.studentName);
        h.rating.setText("Rating: " + s.rating + "/5");
        h.helpful.setText("Helpfulness: " + s.helpfulness + "/5");
        h.comment.setText("Comment: " + s.comment);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        h.date.setText("Date: " + sdf.format(s.timestamp));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView student, rating, helpful, comment, date;

        public ViewHolder(@NonNull View v) {
            super(v);

            student = v.findViewById(R.id.tvSurveyStudent);
            rating = v.findViewById(R.id.tvSurveyRating);
            helpful = v.findViewById(R.id.tvSurveyHelpful);
            comment = v.findViewById(R.id.tvSurveyComment);
            date = v.findViewById(R.id.tvSurveyDate);
        }
    }
}
