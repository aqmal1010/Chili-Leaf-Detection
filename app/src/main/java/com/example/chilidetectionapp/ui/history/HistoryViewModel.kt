package com.example.chilidetectionapp.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.repository.Repository

class HistoryViewModel(private val repository: Repository) : ViewModel() {
    val allDetection: LiveData<List<Detection>> = repository.getAllDetection()

    fun insert(detection: Detection) {
        repository.insertDetection(detection)
    }

    fun delete(detection: Detection) {
        repository.deleteDetection(detection)
    }
}
