package com.example.taskmaster

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    val title: String = "",
    val checked: Boolean = false,
    val key: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable
