package com.example.chilidetectionapp.data

import java.io.Serializable

data class ChiliLeafDisease(
    val name: String,
    val scientificName: String,
    val symptoms: String,
    val cause: String,
    val treatment: String,
    val imageResId: Int
) : Serializable
