package com.example.finalproject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveUser(UserEntity user);

    @Query("SELECT * FROM users LIMIT 1")
    UserEntity getSavedUser();

    @Query("DELETE FROM users")
    void clearUser();
}
