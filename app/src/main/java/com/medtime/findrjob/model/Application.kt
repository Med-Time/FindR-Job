package com.medtime.findrjob.model

data class Application(
    val jobTitle : String? = null,
    val address: String? = null,
    val contact: String? = null,
    val cvUrl: String? = null,
    val email: String? = null,
    val id: String? = null,          // User ID
    val name: String? = null,
    val status: String? = null     // Job provider's feedback
)
