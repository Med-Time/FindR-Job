package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.medtime.findrjob.model.Seeker
import com.medtime.findrjob.model.User

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
    private lateinit var seekerDatabase: DatabaseReference
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
        userId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser?.uid
        Log.d("SeekerAccountDetails", "User ID: $userId")

        // Initialize Firebase references
        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        seekerDatabase = FirebaseDatabase.getInstance().getReference("Seekers").child(userId!!)
        storageReference = FirebaseStorage.getInstance().reference.child("Resumes").child(userId!!)

        // Fetch user and seeker details and populate
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
        // Fetching details from the 'Users' node
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    it.fullname?.let { name -> Log.d("UserDetails", name) }
                    editTextName.setText(it.fullname)
                    editTextEmail.setText(it.email)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserDetails", "Error fetching user details: ${error.message}")
                showToast("Failed to load user details.")
            }
        })

        // Fetching seeker-specific details from the 'Seeker' node
        seekerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val seeker = snapshot.getValue(Seeker::class.java)
                    seeker?.let {
                        editTextSkills.setText(it.skills)
                        editTextEducation.setText(it.education)
                        editTextLocation.setText(it.location)
                        editTextPreferences.setText(it.preferences)
                        Log.d("SeekerDetails", "Education: ${it.education}, Skills: ${it.skills}")

                        // Load profile picture using Glide if URL exists
                        if (it.profilePictureUrl.isNotEmpty()) {
                            loadLogoFromFirebase(it.profilePictureUrl)
                        } else {
                            // Use a placeholder if the URL is empty
                            imageViewProfilePicture.setImageResource(R.drawable.man_dummy)
                        }

                        // Show or hide the resume button based on resume availability
                        viewResumeButton.visibility = if (it.resumeUrl.isNotEmpty()) View.VISIBLE else View.GONE
                    } ?: Log.d("SeekerDetails", "Seeker object is null.")
                } else {
                    Log.d("SeekerDetails", "Snapshot does not exist for userId: $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerDetails", "Error fetching seeker details: ${error.message}")
                showToast("Failed to load seeker details.")
            }
        })
    }

    private fun loadLogoFromFirebase(logoUrl: String) {
        // Load the image from the URL using Glide
        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.man_dummy2) // Default image while loading
            .error(R.drawable.man_dummy) // Default image if loading fails
            .into(imageViewProfilePicture)
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

        val updatedUser = User(
            email = email,
            fullname = name,
            userType = "Job Seeker"
        )

        // Save updated basic user details in 'Users' node
        userDatabase.setValue(updatedUser).addOnCompleteListener {
            if (it.isSuccessful) {
                showToast("User details saved successfully!")
            } else {
                showToast("Failed to save user details.")
            }
        }

        val updatedSeeker = Seeker(skills = skills, education = education,
            location = location, preferences = preferences,
            profilePictureUrl = "", resumeUrl = ""
        )
        // Save seeker-specific details in 'Seeker' node
        seekerDatabase.setValue(updatedSeeker).addOnCompleteListener {
            if (it.isSuccessful) {
                showToast("Seeker details saved successfully!")
            } else {
                showToast("Failed to save seeker details.")
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
                    seekerDatabase.child("resumeUrl").setValue(url.toString())
                    showToast("Resume uploaded successfully!")
                }
            }
            .addOnFailureListener {
                showToast("Failed to upload resume.")
            }
    }

    private fun viewResume() {
        seekerDatabase.child("resumeUrl").get().addOnSuccessListener { snapshot ->
            val resumeUrl = snapshot.getValue(String::class.java)
            if (resumeUrl != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(resumeUrl), "application/pdf")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                    }
                    // Verify there is a PDF viewer app
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        showToast("No PDF viewer app found.")
                    }
                } catch (e: Exception) {
                    showToast("Error opening PDF: ${e.message}")
                }
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
