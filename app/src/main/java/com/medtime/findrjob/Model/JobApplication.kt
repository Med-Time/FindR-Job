package com.medtime.findrjob

data class JobApplication(
    var applicantId: String = "", // Unique ID of the applicant
    val name: String = "",
    val address: String = "",
    val contact: String = "",
    val email:String = "",
    val cvUrl: String = ""
)



