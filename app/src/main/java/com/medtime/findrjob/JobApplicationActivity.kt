package com.medtime.findrjob

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.medtime.findrjob.model.ApplicationData

class JobApplicationActivity : AppCompatActivity() {

    private lateinit var fullName: EditText
    private lateinit var address: EditText
    private lateinit var contactDetail: EditText
    private lateinit var emailAddress: EditText
    private lateinit var submitApplication: Button
    private lateinit var fileButton: Button
    private lateinit var selectedFileName: TextView
    private lateinit var applicationsDatabase: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var fileUri: Uri? = null
    private var userID : String? = null

    companion object {
        private const val PICK_FILE_REQUEST = 1
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5 MB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_application)

        // Initialize UI components
        fullName = findViewById(R.id.fullName)
        address = findViewById(R.id.address)
        contactDetail = findViewById(R.id.contactDetail)
        emailAddress = findViewById(R.id.emailAddress)
        submitApplication = findViewById(R.id.submitApplication)
        fileButton = findViewById(R.id.selectFileButton)
        selectedFileName = findViewById(R.id.selectedFileName)

        // Get job title and job ID from the Intent
        val jobTitle = intent.getStringExtra("jobTitle")
        val jobId = intent.getStringExtra("jobId")
        val companyName = intent.getStringExtra("companyName")

        userID = FirebaseAuth.getInstance().currentUser?.uid

        Log.d("JobApplicationActivity", "Job details: $jobTitle, $jobId, $companyName")

        // Initialize Firebase references
        applicationsDatabase = FirebaseDatabase.getInstance().reference.child("Applications")
        storageReference = FirebaseStorage.getInstance().reference.child("Seekers").child(userID!!)

        // Set up listeners for the buttons
        fileButton.setOnClickListener { selectFile() }
        submitApplication.setOnClickListener {
            if (companyName != null) {
                submitApplication(jobTitle, jobId, companyName)
            }
        }

        // Show dialog to use existing data or enter new data
        showDataPromptDialog(jobTitle, jobId)
    }

    // Show the dialog to prompt the user for saved details or new input
    private fun showDataPromptDialog(jobTitle: String?, jobId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Use Existing Details?")
        builder.setMessage("Do you want to use the saved details or enter new details?")

        builder.setPositiveButton("Use Saved Details") { _, _ ->
            fetchSavedDetails(jobTitle, jobId)
        }

        builder.setNegativeButton("Enter New Details") { _, _ ->
            // Let the user enter details manually
        }

        builder.show()
    }

    // Fetch saved details from SharedPreferences and populate the fields
    private fun fetchSavedDetails(jobTitle: String?, jobId: String?) {
        val sharedPreferences = getSharedPreferences("${userID}Details", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "")
        val addr = sharedPreferences.getString("location", "")
        val contact = sharedPreferences.getString("contactDetail", "+919XXXXXXXXX")
        val email = sharedPreferences.getString("email", "")
        val savedResumeUrl = sharedPreferences.getString("resumeUrl", null)

        if (userID == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        fullName.setText(name)
        address.setText(addr)
        contactDetail.setText(contact)
        emailAddress.setText(email)

        if (!savedResumeUrl.isNullOrEmpty()) {
            fileUri = Uri.parse(savedResumeUrl)
            selectedFileName.text = "Default Resume.pdf"
            Toast.makeText(this, "Details and resume populated with saved data.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Details populated, but no saved resume found.", Toast.LENGTH_SHORT).show()
        }
    }

    // Save the resume URL in SharedPreferences
    private fun saveResumeUrlToSharedPreferences(resumeUrl: String) {
        val sharedPreferences = getSharedPreferences("${userID}Details", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("resumeUrl", resumeUrl)
            apply()
        }
    }

    // Method to submit the application
    private fun submitApplication(jobTitle: String?, jobId: String?, companyName: String) {
        val name = fullName.text.toString().trim()
        val addr = address.text.toString().trim()
        val contact = contactDetail.text.toString().trim()
        val email = emailAddress.text.toString().trim()

        if (name.isEmpty() || addr.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select a valid CV", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || jobId == null || jobTitle == null) {
            Toast.makeText(this, "User or job details are missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val resumeUrl = if (selectedFileName.text == "Default Resume.pdf" && fileUri != null) {
            fileUri.toString() // Use fetched resume URL
        } else {
            null // If no default, proceed with upload
        }

        if (resumeUrl != null) {
            // Save resumeUrl to SharedPreferences
            saveResumeUrlToSharedPreferences(resumeUrl)
            saveApplication(userId, jobId, jobTitle, companyName, name, addr, contact, email, resumeUrl)
        } else {
            val fileRef = storageReference.child("${System.currentTimeMillis()}.pdf")
            fileRef.putFile(fileUri!!).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveResumeUrlToSharedPreferences(uri.toString()) // Save the new resume URL
                    saveApplication(userId, jobId, jobTitle, companyName, name, addr, contact, email, uri.toString())
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get file URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "CV upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



    // Method to select a file (CV)
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Select CV"), PICK_FILE_REQUEST)
    }

    // Handle file selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data?.data != null) {
            fileUri = data.data
            val fileName = getFileName(fileUri)
            selectedFileName.text = fileName ?: "No file selected"

            // Check file size
            fileUri?.let {
                val returnCursor: Cursor? = contentResolver.query(it, null, null, null, null)
                returnCursor?.use {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    it.moveToFirst()
                    val fileSize = it.getLong(sizeIndex)
                    if (fileSize > MAX_FILE_SIZE) {
                        Toast.makeText(this, "File is too large to upload. Max size is 5 MB.", Toast.LENGTH_LONG).show()
                        fileUri = null
                    }
                }
            }
        } else {
            Toast.makeText(this, "File selection failed!", Toast.LENGTH_SHORT).show()
        }
    }

    // Get the file name from the URI
    private fun getFileName(uri: Uri?): String? {
        var name: String? = null
        if (uri != null) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    it.moveToFirst()
                    name = it.getString(columnIndex)
                }
            }
        }
        return name
    }

    // Save application data and show dialog
    private fun saveApplication(
        userId: String,
        jobId: String,
        jobTitle: String,
        companyName: String,
        name: String,
        addr: String,
        contact: String,
        email: String,
        resumeUrl: String
    ) {

        val id = applicationsDatabase.push().key
        if (id != null) {
            val applicationData = ApplicationData(jobId, jobTitle,companyName, name, addr, contact, email, resumeUrl)
            applicationsDatabase.child(userId).child(id).setValue(applicationData).addOnCompleteListener {
                if (it.isSuccessful) {
                    showApplicationSubmittedDialog()
                } else {
                    Toast.makeText(this, "Failed to submit application.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showApplicationSubmittedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Application Submitted")
        builder.setIcon(R.drawable.ic_check_circle)
        builder.setMessage("The company will contact you shortly.")
        builder.setPositiveButton("OK") { _, _ ->
            val intent = Intent(this, JobSeekerDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }
}
