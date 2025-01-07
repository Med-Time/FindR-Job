package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.medtime.findrjob.Model.Seeker

class SeekerAccountDetails : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextSkills: EditText
    private lateinit var editTextEducation: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextPreferences: EditText
    private lateinit var buttonEdit: Button
    private lateinit var buttonSave: Button
    private lateinit var btnResetPassword: Button
    private lateinit var viewResumeButton: Button
    private lateinit var uploadResumeButton: Button
    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var userDatabase: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var userId: String? = null
    private var resumeUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seeker_account_details)
        setupEdgeInsets()

        // Initialize views
        initializeViews()

        // Get userId from intent
        userId = intent.getStringExtra("userId")
        if (userId.isNullOrEmpty()) {
            showToast("User ID not found.")
            finish()
            return
        }

        // Initialize Firebase references
        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        storageReference = FirebaseStorage.getInstance().reference.child("Resumes").child(userId!!)

        // Fetch user details and populate
        fetchUserDetails()

        // Set button click listeners
        setListeners()
    }

    private fun initializeViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextSkills = findViewById(R.id.editTextSkills)
        editTextEducation = findViewById(R.id.editTextEducation)
        editTextLocation = findViewById(R.id.editTextLocation)
        editTextPreferences = findViewById(R.id.editTextPreferences)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonSave = findViewById(R.id.buttonSave)
        btnResetPassword = findViewById(R.id.resetPasswordButton)
        viewResumeButton = findViewById(R.id.viewResumeButton)
        uploadResumeButton = findViewById(R.id.resume)
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture)
    }

    private fun setupEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchUserDetails() {
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Seeker::class.java)
                user?.let {
                    editTextName.setText(it.name)
                    editTextEmail.setText(it.email)
                    editTextSkills.setText(it.skills)
                    editTextEducation.setText(it.education)
                    editTextLocation.setText(it.location)
                    editTextPreferences.setText(it.preferences)

                    // Load profile picture
                    if (it.profilePictureUrl.isNotEmpty()) {
                        val uri = Uri.parse(it.profilePictureUrl)
                        imageViewProfilePicture.setImageURI(uri)
                    }

                    // Load resume URL
                    if (it.resumeUrl.isNotEmpty()) {
                        viewResumeButton.visibility = View.VISIBLE
                    } else {
                        viewResumeButton.visibility = View.GONE
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load user details.")
            }
        })
    }

    private fun setListeners() {
        buttonEdit.setOnClickListener {
            enableEditing(true)
        }

        buttonSave.setOnClickListener {
            saveUserData()
        }

        btnResetPassword.setOnClickListener {
            resetPassword()
        }

        viewResumeButton.setOnClickListener {
            viewResume()
        }

        uploadResumeButton.setOnClickListener {
            chooseResume()
        }

        enableEditing(false)
    }

    private fun enableEditing(enable: Boolean) {
        editTextName.isEnabled = enable
        editTextEmail.isEnabled = enable
        editTextSkills.isEnabled = enable
        editTextEducation.isEnabled = enable
        editTextLocation.isEnabled = enable
        editTextPreferences.isEnabled = enable
        buttonEdit.visibility = if (enable) View.GONE else View.VISIBLE
        buttonSave.visibility = if (enable) View.VISIBLE else View.GONE
        uploadResumeButton.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun saveUserData() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val skills = editTextSkills.text.toString().trim()
        val education = editTextEducation.text.toString().trim()
        val location = editTextLocation.text.toString().trim()
        val preferences = editTextPreferences.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            showToast("Name and Email cannot be empty!")
            return
        }

        val updatedUser = Seeker(
            name, email, skills, education, location, preferences,
            profilePictureUrl = "",
            resumeUrl = ""
        )

        userDatabase.setValue(updatedUser).addOnCompleteListener {
            if (it.isSuccessful) {
                showToast("Details saved successfully!")
            } else {
                showToast("Failed to save details.")
            }
        }

        resumeUri?.let { uploadResume(it) }
        enableEditing(false)
    }

    private fun resetPassword() {
        val email = editTextEmail.text.toString().trim()
        if (email.isEmpty()) {
            showToast("Email field is empty.")
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset email sent.")
                } else {
                    showToast("Failed to send password reset email.")
                }
            }
    }

    private fun chooseResume() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), STORAGE_REQUEST_CODE)
    }

    private fun uploadResume(uri: Uri) {
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { url ->
                    userDatabase.child("resumeUrl").setValue(url.toString())
                    showToast("Resume uploaded successfully!")
                }
            }
            .addOnFailureListener {
                showToast("Failed to upload resume.")
            }
    }

    private fun viewResume() {
        userDatabase.child("resumeUrl").get().addOnSuccessListener { snapshot ->
            val resumeUrl = snapshot.getValue(String::class.java)
            if (resumeUrl != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl))
                startActivity(intent)
            } else {
                showToast("No resume available.")
            }
        }.addOnFailureListener {
            showToast("Failed to load resume.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            resumeUri = data?.data
            if (resumeUri != null) {
                showToast("Resume selected.")
            }
        }
    }

    companion object {
        private const val STORAGE_REQUEST_CODE = 100
    }
}
