package com.example.chilidetectionapp.di

import android.content.Context
import com.example.chilidetectionapp.data.DetectionRoomDatabase
import com.example.chilidetectionapp.repository.Repository

object Injection {
    fun provideRepository(context: Context): Repository {
        val database = DetectionRoomDatabase.getDatabase(context)
        return Repository(database)
    }
}