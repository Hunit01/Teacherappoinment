package com.example.finalproject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppointmentDao {

    // STUDENT: all appointments
    @Query("SELECT * FROM appointments " +
            "WHERE studentId = :studentId " +
            "ORDER BY createdAt DESC")
    List<Appointment> getAppointmentsForStudent(String studentId);

    // TEACHER: by single status (Pending / Declined / Cancelled)
    @Query("SELECT * FROM appointments " +
            "WHERE teacherId = :teacherId AND status = :status " +
            "ORDER BY createdAt DESC")
    List<Appointment> getAppointmentsForTeacherByStatus(String teacherId, String status);

    // TEACHER: Approved & NOT completed
    @Query("SELECT * FROM appointments " +
            "WHERE teacherId = :teacherId " +
            "AND status = 'Approved' AND completed = 0 " +
            "ORDER BY createdAt DESC")
    List<Appointment> getTeacherApprovedNotCompleted(String teacherId);

    // TEACHER: Completed (status Completed)
    @Query("SELECT * FROM appointments " +
            "WHERE teacherId = :teacherId " +
            "AND status = 'Completed' " +
            "ORDER BY createdAt DESC")
    List<Appointment> getTeacherCompleted(String teacherId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Appointment> appointments);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Appointment appointment);
}
