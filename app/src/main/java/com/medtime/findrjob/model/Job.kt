package com.medtime.findrjob.model

data class Job(
    var id: String = "",
    var company:String = "",
    var logoUrl: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val salary: String = "",
    val skills: String = "",
    val providerId: String = ""
)
