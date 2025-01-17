package com.medtime.findrjob.model

data class ApplicationData(
    val userID: String,
    val jobId: String,
    val jobTitle: String,
    val name: String,
    val address: String,
    val contact: String,
    val email: String,
    val fileUrl: String
)

