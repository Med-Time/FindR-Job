package com.medtime.findrjob

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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

    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences = getSharedPreferences("IgnoredJobs", 0)

        // Get job title and job ID from the Intent
        val jobTitle = intent.getStringExtra("jobTitle")
        val jobId = intent.getStringExtra("jobId")
        val companyName = intent.getStringExtra("companyName")

        Log.d("JobApplicationActivity", "Job details: $jobTitle, $jobId, $companyName")

        // Initialize Firebase references
        applicationsDatabase = FirebaseDatabase.getInstance().reference.child("Applications")
        storageReference = FirebaseStorage.getInstance().reference.child("CVs")

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
        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "")
        val addr = sharedPreferences.getString("location", "")
        val contact = sharedPreferences.getString("contactDetail", "+919XXXXXXXXX")
        val email = sharedPreferences.getString("email", "")

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch additional details from "Seekers" table in Firebase
        val seekersDatabase = FirebaseDatabase.getInstance().reference.child("Seekers").child(userID)
        seekersDatabase.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val resumeUrl = snapshot.child("resumeUrl").value as? String
                val education = snapshot.child("education").value as? String
                val skills = snapshot.child("skills").value as? String

                // Use saved data if available
                fullName.setText(name)
                address.setText(addr)
                contactDetail.setText(contact)
                emailAddress.setText(email)

                if (!resumeUrl.isNullOrEmpty()) {
                    fileUri = Uri.parse(resumeUrl)
                    selectedFileName.text = "Default Resume.pdf"
                    Toast.makeText(this, "Details and resume populated with saved data.", Toast.LENGTH_SHORT).show()
                } else {
                    selectedFileName.text = "No default resume found"
                    Toast.makeText(this, "Details populated, but no saved resume found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No saved details found in Seekers table. Please enter manually.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch details: ${e.message}", Toast.LENGTH_SHORT).show()
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
            saveApplication(userId, jobId, jobTitle,companyName, name, addr, contact, email, resumeUrl)
        } else {
            val fileRef = storageReference.child("${System.currentTimeMillis()}.pdf")
            fileRef.putFile(fileUri!!).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveApplication(userId, jobId, jobTitle,companyName, name, addr, contact, email, uri.toString())
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get file URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "CV upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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

        Log.d("JobApplicationActivity", "Saving application: $userId, $jobId, $jobTitle, $companyName")

        val sharedPreferences = getSharedPreferences("IgnoredJobs", 0)
        val ignoredJobs = sharedPreferences.getStringSet("ignoredJobs", mutableSetOf())!!
        ignoredJobs.add(jobId)
        sharedPreferences.edit().putStringSet("ignoredJobs", ignoredJobs).apply()

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

    // Show dialog and finish activity
    private fun showApplicationSubmittedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Application Submitted")
        builder.setMessage("The company will contact you shortly.")
        builder.setPositiveButton("OK") { _, _ ->
            startActivity(Intent(this, JobSeekerDashboard::class.java))
            finish() // Finish activity and return to previous fragment
        }
        builder.setCancelable(false)
        builder.show()
    }


    // Method to download and open the CV
    private fun downloadAndOpenCV(fileUrl: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle("Downloading CV")
            setDescription("Downloading CV from Firebase Storage")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CV.pdf")
        }
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
