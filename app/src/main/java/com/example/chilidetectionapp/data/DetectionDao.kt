package com.example.chilidetectionapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DetectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detection: Detection)

    @Query("SELECT * FROM detection ORDER BY id DESC")
    fun getAllDetections(): LiveData<List<Detection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetection(detection: Detection)

    @Delete
    fun deleteDetection(detection: Detection)
}