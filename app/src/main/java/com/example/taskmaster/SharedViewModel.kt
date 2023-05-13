package com.example.taskmaster

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class SharedViewModel : ViewModel() {
    val selectedDate = MutableLiveData<Calendar>().apply { value = Calendar.getInstance() }
}
