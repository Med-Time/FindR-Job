package com.medtime.findrjob.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Application(
    val jobId: String?="",
    val jobTitle: String = "",
    val company: String = "",
    val name: String? = "",
    val address: String? = "",
    val contact: String? = "",
    val email: String? = "",
    val fileUrl: String? = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val status: String = "Pending",
    val message: String = ""
)