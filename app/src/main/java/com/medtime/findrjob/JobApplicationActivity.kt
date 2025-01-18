package com.medtime.findrjob

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Initialize Firebase references
        applicationsDatabase = FirebaseDatabase.getInstance().reference.child("Applications")
        storageReference = FirebaseStorage.getInstance().reference.child("CVs")

        // Set up listeners for the buttons
        fileButton.setOnClickListener { selectFile() }
        submitApplication.setOnClickListener { submitApplication(jobTitle, jobId) }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Select CV"), PICK_FILE_REQUEST)
    }

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

    private fun submitApplication(jobTitle: String?, jobId: String?) {
        val name = fullName.text.toString().trim()
        val addr = address.text.toString().trim()
        val contact = contactDetail.text.toString().trim()
        val email = emailAddress.text.toString().trim()

        // Validate input
        if (name.isEmpty() || addr.isEmpty() || contact.isEmpty() || email.isEmpty() || fileUri == null) {
            Toast.makeText(this, "Please fill all fields and select a valid CV", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure jobId and userID are available
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID == null || jobId == null || jobTitle == null) {
            Toast.makeText(this, "User or job details are missing.", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload the CV to Firebase Storage
        val fileRef = storageReference.child("${System.currentTimeMillis()}.pdf")
        fileUri?.let {
            fileRef.putFile(it).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val fileUrl = uri.toString()

                    // Generate a new application ID and store application data
                    val id = applicationsDatabase.push().key
                    if (id != null) {
                        val applicationData = ApplicationData(
                            userID, jobId, jobTitle, name, addr, contact, email, fileUrl
                        )
                        applicationsDatabase.child(id).setValue(applicationData)
                        Toast.makeText(this, "Application Submitted. The Company will contact you shortly.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to generate application ID.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get file URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "CV upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

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
