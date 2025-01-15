package com.medtime.findrjob.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object FirebaseStorageHelper {
    private val storage = FirebaseStorage.getInstance()

    fun uploadFile(fileUri: Uri, path: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = storage.reference.child(path)
        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}