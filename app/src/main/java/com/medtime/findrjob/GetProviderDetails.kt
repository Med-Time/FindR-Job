package com.medtime.findrjob

import android.app.Activity
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

class GetProviderDetails : AppCompatActivity() {

    private lateinit var imageViewLogo: ImageView
    private lateinit var editTextCompanyName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var editTextIndustryType: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var userID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_provider_details)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Providers")
        storageReference = FirebaseStorage.getInstance().getReference("Providers")

        userID = (intent.getStringExtra("userID")?: firebaseAuth.currentUser?.uid).toString()

        // Initialize views
        imageViewLogo = findViewById(R.id.imageViewLogo)
        editTextCompanyName = findViewById(R.id.editTextCompanyName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextAddress = findViewById(R.id.editTextAddress)
        editTextIndustryType = findViewById(R.id.editTextIndustryType)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        val buttonUploadLogo: Button = findViewById(R.id.buttonUploadLogo)

        // Image Picker Launcher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedImageUri = data?.data
                imageViewLogo.setImageURI(selectedImageUri)
            }
        }

        // Upload Logo Button
        buttonUploadLogo.setOnClickListener {
            pickImageFromGallery()
        }

        // Submit Button
        buttonSubmit.setOnClickListener {
            val companyName = editTextCompanyName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val address = editTextAddress.text.toString().trim()
            val industryType = editTextIndustryType.text.toString().trim()

            // Validate inputs
            if (companyName.isEmpty()) {
                showToast("Please enter your company name")
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                showToast("Please enter your email address")
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                showToast("Please enter your office address")
                return@setOnClickListener
            }
            if (industryType.isEmpty()) {
                showToast("Please enter your industry type")
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                showToast("Please upload your company logo")
                return@setOnClickListener
            }

            // Save to Firebase
            uploadLogoAndSaveDetails(companyName, email, address, industryType)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun uploadLogoAndSaveDetails(
        companyName: String,
        email: String,
        address: String,
        industryType: String
    ) {
        // Show a loading message
        buttonSubmit.isEnabled = false
        showToast("Saving details...")


        // Upload logo to Firebase Storage
        val logoRef = storageReference.child("$userID/logo.jpg")
        selectedImageUri?.let { uri ->
            logoRef.putFile(uri)
                .addOnSuccessListener {
                    // Get the logo's download URL
                    logoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProviderDetailsToFirebase(
                            companyName, email, address, industryType, downloadUri.toString()
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to upload logo: ${exception.message}")
                    buttonSubmit.isEnabled = true
                }
        }
    }

    private fun saveProviderDetailsToFirebase(
        companyName: String,
        email: String,
        address: String,
        industryType: String,
        logoUrl: String
    ) {
    val providerDetails = mapOf(
        "companyName" to companyName,
        "email" to email,
        "address" to address,
        "industryType" to industryType,
        "logoUrl" to logoUrl
    )
    databaseReference.child(userID).setValue(providerDetails)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Details saved successfully!")
                navigateToDashboard()
            } else {
                showToast("Failed to save details: ${task.exception?.message}")
                buttonSubmit.isEnabled = true
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, newjobproviderdashboard::class.java)
        intent.putExtra("userId", userID)
        startActivity(intent)
        finish()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}