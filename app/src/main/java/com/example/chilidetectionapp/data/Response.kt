package com.example.chilidetectionapp.data

data class DiseaseDataResponse(
    val disease_name: String,
    val scientific_name: String,
    val symptoms: String,
    val causes: String,
    val treatment: Treatment
)

data class Treatment(
    val hayati: String,
    val kimiawi: String
)
