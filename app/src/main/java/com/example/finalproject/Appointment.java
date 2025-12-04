package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {

    @PrimaryKey
    @NonNull
    public String appointmentId;

    public String teacherId;
    public String teacherName;

    public String studentId;
    public String studentName;
    public String studentClass;

    public String date;      // "2025-11-20"
    public String timeSlot;  // "6:20 PM"
    public String message;

    public String declineReason = "";

    public String notesUrl = "";
    public String notesMessage = "";
    public long notesUploadedAt = 0;


    // "Pending", "Approved", "Declined", "Cancelled", "Completed"
    public String status;

    public long createdAt;

    // used to show "Rescheduled"
    public boolean rescheduled;

    // NEW FIELDS
    public boolean completed;        // true = session done
    public boolean surveyPending;    // true = student can fill survey
    public boolean surveyCompleted;  // true = survey already submitted
    public String cancelledBy;       // "Student", "Teacher", etc.

    public Appointment() {
        // Firestore + Room need empty constructor
    }

    public Appointment(String appointmentId,
                       String teacherId,
                       String teacherName,
                       String studentId,
                       String studentName,
                       String studentClass,
                       String date,
                       String timeSlot,
                       String message,
                       String status,
                       long createdAt,
                       boolean rescheduled) {

        this.appointmentId = appointmentId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.date = date;
        this.timeSlot = timeSlot;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.rescheduled = rescheduled;

        // Defaults
        this.completed = false;
        this.surveyPending = false;
        this.surveyCompleted = false;
        this.cancelledBy = "";
        this.declineReason = "";
        this.notesUrl = "";


    }
}
