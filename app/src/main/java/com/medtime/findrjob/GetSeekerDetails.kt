package com.medtime.findrjob

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class GetSeekerDetails : AppCompatActivity() {

    private lateinit var imageViewLogo: ImageView
    private lateinit var editTextEducation: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextSkills: EditText
    private lateinit var editTextPreferences: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonUploadResume: Button
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickResumeLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private var selectedResumeUri: Uri? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var userID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_seeker_details)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Seekers")
        storageReference = FirebaseStorage.getInstance().reference

        userID = intent.getStringExtra("userID") ?: firebaseAuth.currentUser?.uid

        // Initialize views
        imageViewLogo = findViewById(R.id.imageViewLogo)
        editTextEducation = findViewById(R.id.education)
        editTextLocation = findViewById(R.id.location)
        editTextSkills = findViewById(R.id.skills)
        editTextPreferences = findViewById(R.id.preferences)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        val buttonUploadLogo: Button = findViewById(R.id.buttonUploadLogo)
        buttonUploadResume = findViewById(R.id.resume)

        // Image Picker Launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data
                imageViewLogo.setImageURI(selectedImageUri)
            }
        }

        // Resume Picker Launcher
        pickResumeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedResumeUri = data?.data
                showToast("Resume selected successfully!")
            }
        }

        // Upload Image Button
        buttonUploadLogo.setOnClickListener {
            pickImageFromGallery()
        }

        // Upload Resume Button
        buttonUploadResume.setOnClickListener {
            pickResumeFromStorage()
        }

        // Submit Button
        buttonSubmit.setOnClickListener {
            val education = editTextEducation.text.toString().trim()
            val location = editTextLocation.text.toString().trim()
            val skills = editTextSkills.text.toString().trim()
            val preferences = editTextPreferences.text.toString().trim()

            if (education.isEmpty() || location.isEmpty() || skills.isEmpty() || preferences.isEmpty()) {
                showToast("All fields are required!")
                return@setOnClickListener
            }

            if (userID != null) {
                saveSeekerDetails(userID!!, education, location, skills, preferences)
            } else {
                showToast("User not found!")
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun pickResumeFromStorage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/pdf" }
        pickResumeLauncher.launch(intent)
    }

    private fun saveSeekerDetails(
        userID: String,
        education: String,
        location: String,
        skills: String,
        preferences: String
    ) {
        buttonSubmit.isEnabled = false
        showToast("Saving details...")

        val imageRef = storageReference.child("Seekers/$userID/profile_picture.jpg")
        val resumeRef = storageReference.child("Seekers/$userID/resume.pdf")

        // Upload Profile Picture
        selectedImageUri?.let { imageUri ->
            imageRef.putFile(imageUri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    // Upload Resume
                    selectedResumeUri?.let { resumeUri ->
                        resumeRef.putFile(resumeUri).addOnSuccessListener {
                            resumeRef.downloadUrl.addOnSuccessListener { resumeUrl ->
                                val seekerDetails = mapOf(
                                    "education" to education,
                                    "location" to location,
                                    "skills" to skills,
                                    "preferences" to preferences,
                                    "profilePictureUrl" to imageUrl.toString(),
                                    "resumeUrl" to resumeUrl.toString()
                                )

                                // Save to Firebase
                                databaseReference.child(userID).setValue(seekerDetails)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            saveDetailsLocally(
                                                education,
                                                location,
                                                skills,
                                                preferences,
                                                imageUrl.toString(),
                                                resumeUrl.toString()
                                            )
                                            showToast("Details saved successfully!")
                                            navigateToSeekerDashboard()
                                        } else {
                                            showToast("Failed to save details!")
                                            buttonSubmit.isEnabled = true
                                        }
                                    }
                            }
                        }.addOnFailureListener {
                            showToast("Failed to upload resume!")
                            buttonSubmit.isEnabled = true
                        }
                    }
                }
            }.addOnFailureListener {
                showToast("Failed to upload profile picture!")
                buttonSubmit.isEnabled = true
            }
        }
    }

    private fun saveDetailsLocally(
        education: String,
        location: String,
        skills: String,
        preferences: String,
        profilePictureUrl: String,
        resumeUrl: String
    ) {
        val sharedPreferences = getSharedPreferences("${userID}Details", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("education", education)
        editor.putString("location", location)
        editor.putString("skills", skills)
        editor.putString("preferences", preferences)
        editor.putString("profilePictureUrl", profilePictureUrl)
        editor.putString("resumeUrl", resumeUrl)
        editor.apply()
    }

    private fun navigateToSeekerDashboard() {
        val intent = Intent(this, JobSeekerDashboard::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
