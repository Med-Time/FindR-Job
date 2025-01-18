package com.medtime.findrjob.model
//data class for posting a job
data class JobPostData(
    val title: String = "",
    val description: String = "",
    val skills: String = "",
    val salary: String = "",
    val date: String = "",
//    val providerId: String = "" // To fetch provider-specific data
)
