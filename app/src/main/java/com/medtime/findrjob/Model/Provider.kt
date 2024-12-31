package com.medtime.findrjob.Model

data class Provider(
    var companyName: String = "",
    var email: String = "",
    var address: String = "",
    var industryType: String = "",
    var logoUrl: String = "" // URL for the uploaded company logo
)
