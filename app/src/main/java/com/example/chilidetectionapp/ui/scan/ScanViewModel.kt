package com.example.chilidetectionapp.ui.scan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.repository.Repository
import kotlinx.coroutines.launch

class ScanViewModel(private val repository: Repository) : ViewModel() {
    fun insertDetection(detection: Detection) {
        viewModelScope.launch {
            repository.insertDetection(detection)
            Log.d("ScanViewModel", "Detection inserted: $detection")
        }
    }
}