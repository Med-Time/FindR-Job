package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

class SeekerAccountDetails : BaseActivity() {

    private lateinit var editTextName: TextView
    private lateinit var editTextEmail: TextView
    private lateinit var editTextSkills: TextView
    private lateinit var editSkillsIcon: ImageView
    private lateinit var editTextEducation: TextView
    private lateinit var editEducationIcon: ImageView
    private lateinit var editTextLocation: TextView
    private lateinit var editLocationIcon: ImageView
    private lateinit var editTextPreferences: TextView
    private lateinit var editPreferencesIcon: ImageView
    private lateinit var imageViewProfilePicture: ImageView
    private lateinit var buttonEditProfileImage: Button
    private lateinit var buttonSave: Button
    private lateinit var viewResumeButton: Button
    private lateinit var editResumeIcon: ImageView
    private lateinit var userDatabase: DatabaseReference
    private lateinit var seekerDatabase: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var userId: String? = null
    private var resumeUri: Uri? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seeker_account_details)
        setupEdgeInsets()

        // Initialize views
        initializeViews()

        // Get userId from intent or current user
        userId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser?.uid
        Log.d("SeekerAccountDetails", "User ID: $userId")

        // Initialize Firebase references
        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        seekerDatabase = FirebaseDatabase.getInstance().getReference("Seekers").child(userId!!)
        storageReference = FirebaseStorage.getInstance().reference.child("Profiles").child(userId!!)

        // Fetch user and seeker details and populate
        fetchUserDetails()

        // Set button click listeners
        setListeners()
    }

    private fun initializeViews() {
        editTextName = findViewById(R.id.textViewFullName)
        editTextEmail = findViewById(R.id.textViewEmail)
        editTextSkills = findViewById(R.id.textViewSkills)
        editSkillsIcon = findViewById(R.id.editSkillsIcon)
        editTextEducation = findViewById(R.id.textViewEducation)
        editEducationIcon = findViewById(R.id.editEducationIcon)
        editTextLocation = findViewById(R.id.textViewLocation)
        editLocationIcon = findViewById(R.id.editLocationIcon)
        editTextPreferences = findViewById(R.id.textViewPreferences)
        editPreferencesIcon = findViewById(R.id.editPreferencesIcon)
        buttonEditProfileImage = findViewById(R.id.buttonEditProfileImage)
        buttonSave = findViewById(R.id.buttonSaveChanges)
        viewResumeButton = findViewById(R.id.viewResumeButton)
        editResumeIcon = findViewById(R.id.editResumeIcon)
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
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    editTextName.text = it.fullname
                    editTextEmail.text = it.email
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserDetails", "Error fetching user details: ${error.message}")
                showToast("Failed to load user details.")
            }
        })

        seekerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val seeker = snapshot.getValue(Seeker::class.java)
                    seeker?.let {
                        editTextSkills.text = it.skills
                        editTextEducation.text = it.education
                        editTextLocation.text = it.location
                        editTextPreferences.text = it.preferences

                        if (it.profilePictureUrl.isNotEmpty()) {
                            loadLogoFromFirebase(it.profilePictureUrl)
                        } else {
                            imageViewProfilePicture.setImageResource(R.drawable.man_dummy)
                        }

                        viewResumeButton.visibility =
                            if (it.resumeUrl.isNotEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerDetails", "Error fetching seeker details: ${error.message}")
                showToast("Failed to load seeker details.")
            }
        })
    }

    private fun loadLogoFromFirebase(logoUrl: String) {
        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.man_dummy)
            .error(R.drawable.man_dummy2)
            .into(imageViewProfilePicture)
    }

    private fun setListeners() {
        buttonEditProfileImage.setOnClickListener {
            chooseProfileImage()
        }
        buttonSave.setOnClickListener {
            saveUserData()
        }
        viewResumeButton.setOnClickListener {
            viewResume()
        }

        // Edit icons click listeners
        editSkillsIcon.setOnClickListener { enableEditing(editTextSkills, "skills") }
        editEducationIcon.setOnClickListener { enableEditing(editTextEducation, "education") }
        editLocationIcon.setOnClickListener { enableEditing(editTextLocation, "location") }
        editPreferencesIcon.setOnClickListener { enableEditing(editTextPreferences, "preferences") }
        editResumeIcon.setOnClickListener { chooseResume() }
    }

    private fun enableEditing(textView: TextView, field: String) {
        buttonSave.visibility = View.VISIBLE
        val currentText = textView.text.toString()

        val editText = EditText(this).apply {
            setText(currentText)
            layoutParams = textView.layoutParams
        }

        val parent = textView.parent as ViewGroup
        val index = parent.indexOfChild(textView)
        parent.removeView(textView)
        parent.addView(editText, index)

        editText.requestFocus()
        editText.setSelection(currentText.length)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) saveChanges(editText, textView, field)
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveChanges(editText, textView, field)
                true
            } else {
                false
            }
        }
    }

    private fun saveChanges(editText: EditText, textView: TextView, field: String) {
        val updatedText = editText.text.toString().trim()

        val parent = editText.parent as ViewGroup
        val index = parent.indexOfChild(editText)
        parent.removeView(editText)
        textView.text = updatedText
        parent.addView(textView, index)

        seekerDatabase.child(field).setValue(updatedText).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("$field updated successfully!")
            } else {
                showToast("Failed to update $field.")
            }
        }
    }

    private fun chooseProfileImage() {
        // Launch an intent to pick an image from the gallery
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Profile Image"),
            PROFILE_IMAGE_REQUEST_CODE
        )
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

        // Update Seeker info in the database
        seekerDatabase.child("skills").setValue(skills)
        seekerDatabase.child("education").setValue(education)
        seekerDatabase.child("location").setValue(location)
        seekerDatabase.child("preferences").setValue(preferences)

        // Update profile image if a new image is selected
        selectedImageUri?.let { uploadProfileImage(it) }

        // Hide keyboard and clear focus from any focused view
        val currentFocusView = currentFocus
        currentFocusView?.clearFocus() // Clear focus from the current view
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocusView?.windowToken, 0)

        showToast("Changes saved successfully!")
    }


    private fun uploadProfileImage(uri: Uri) {
        storageReference.putFile(uri)
            .addOnSuccessListener {
                // Get the download URL after uploading
                storageReference.downloadUrl.addOnSuccessListener { url ->
                    // Update URL in the seeker database
                    seekerDatabase.child("profilePictureUrl").setValue(url.toString())
                    showToast("Profile image uploaded successfully!")
                    loadLogoFromFirebase(url.toString()) // Load the new image
                }
            }
            .addOnFailureListener {
                showToast("Failed to upload profile image.")
            }
    }

    private fun chooseResume() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Resume"), STORAGE_REQUEST_CODE)
    }

    private fun viewResume() {
        seekerDatabase.child("resumeUrl").get().addOnSuccessListener { snapshot ->
            val resumeUrl = snapshot.getValue(String::class.java)
            if (resumeUrl != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(resumeUrl), "application/pdf")
                        flags =
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                    }
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

        // Handle profile image selection
        if (requestCode == PROFILE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                showToast("Profile image selected.")
                imageViewProfilePicture.setImageURI(selectedImageUri) // Show selected image
            }
        }

        // Handle resume selection
        if (requestCode == STORAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            resumeUri = data?.data
            if (resumeUri != null) {
                showToast("Resume selected.")
            }
        }
    }

    companion object {
        private const val STORAGE_REQUEST_CODE = 100
        private const val PROFILE_IMAGE_REQUEST_CODE = 101
    }
}