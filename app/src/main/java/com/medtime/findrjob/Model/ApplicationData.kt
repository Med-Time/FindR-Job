package com.medtime.Model

data class ApplicationData(
    var name: String = "",
    var address: String = "",
    var contact: String = "",
    var email: String = "",
    var applicantId: String = "",
    var providerId: String="",
    var cvUrl: String = ""  // Added to store the CV URL
)
