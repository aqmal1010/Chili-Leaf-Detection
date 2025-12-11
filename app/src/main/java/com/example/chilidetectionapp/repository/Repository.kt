package com.example.chilidetectionapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.data.DetectionRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val detectionRoomDatabase: DetectionRoomDatabase) {
    private val detectionDao = detectionRoomDatabase.detectionDao()

    private var _detection = MutableLiveData<List<Detection>>()
    var detection: LiveData<List<Detection>> = _detection

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getAllDetection(): LiveData<List<Detection>> {
        return detectionDao.getAllDetections()
    }

    fun insertDetection(detection: Detection) {
        Thread {
            detectionDao.insertDetection(detection)
        }.start()
    }

    fun deleteDetection(detection: Detection) {
        Thread {
            detectionDao.deleteDetection(detection)
        }.start()
    }
}