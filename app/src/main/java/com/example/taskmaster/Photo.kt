package com.example.taskmaster

// File: Photo.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val timestamp: Long
)
