package com.medtime.findrjob.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ProfileData(
    val name: String,
    val appliedCount: Int,
    val acceptedCount: Int,
    val rejectedCount: Int
)

class ProfileView : ViewModel() {
    private val _profileData = MutableLiveData<ProfileData>()
    val profileData: LiveData<ProfileData> get() = _profileData

    // Update profile data
    fun updateProfile(name: String, appliedCount: Int, acceptedCount: Int, rejectedCount: Int) {
        _profileData.value = ProfileData(name, appliedCount, acceptedCount, rejectedCount)
    }
}
