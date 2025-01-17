package com.medtime.findrjob.model

data class Application(
    val jobTitle : String? = null,
    val address: String? = null,
    val contact: String? = null,
    val cvUrl: String? = null,
    val email: String? = null,
    val jobID: String? = null,
    val status: String? = null,
    var company: String? = null,
    val date: String? = null
)
