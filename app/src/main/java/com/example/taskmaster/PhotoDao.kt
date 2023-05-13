package com.example.taskmaster

// File: PhotoDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insertPhoto(photo: Photo)

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotos(): List<Photo>
}
