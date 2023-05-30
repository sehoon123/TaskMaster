package com.example.taskmaster

// File: PhotoDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PhotoDao {
    @Insert
    fun insertPhoto(photo: Photo)

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): List<Photo>

    @Update
    fun updatePhoto(photo: Photo)
}
