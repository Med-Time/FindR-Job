package com.medtime.findrjob.model

data class JobPostData(
    val title: String = "",
    val salary: String = "",
    val skills: String = "",
    val date: String = "",
    val description: String = "",
    val providerId: String = "" // To fetch provider-specific data
)
