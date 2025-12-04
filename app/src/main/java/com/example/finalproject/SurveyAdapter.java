package com.example.finalproject;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.ViewHolder> {

    Context context;
    ArrayList<Survey> list;

    public SurveyAdapter(Context context, ArrayList<Survey> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_survey, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Survey s = list.get(pos);

        h.tvStudentName.setText("Student: " + s.studentName);
        h.ratingBar.setRating(s.rating);
        h.helpfulBar.setRating(s.helpfulness);

        if (s.comment == null || s.comment.trim().isEmpty()) {
            h.tvComment.setText("Comment: (none)");
        } else {
            h.tvComment.setText("Comment: " + s.comment);
        }

        // Format timestamp → readable date
        String formatted = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(s.timestamp)).toString();
        h.tvDate.setText("Submitted: " + formatted);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvStudentName, tvComment, tvDate;
        RatingBar ratingBar, helpfulBar;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvStudentName = v.findViewById(R.id.tvStudentName);
            tvComment = v.findViewById(R.id.tvComment);
            tvDate = v.findViewById(R.id.tvDate);

            ratingBar = v.findViewById(R.id.ratingBarSurvey);
            helpfulBar = v.findViewById(R.id.helpfulBarSurvey);
        }
    }
}
