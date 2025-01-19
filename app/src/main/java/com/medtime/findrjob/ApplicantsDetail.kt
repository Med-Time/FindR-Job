package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ApplicantsDetail : BaseActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var contactDetailTextView: TextView
    private lateinit var emailAddressTextView: TextView
    private lateinit var reasonEditText: EditText
    private lateinit var acceptButton: Button
    private lateinit var rejectButton: Button
    private lateinit var viewResumeButton: Button

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_applicants_detail)
        supportActionBar?.title = "Applicant Details"
        setSearchQueryListener { }

        // Retrieve data from Intent
        val userID = intent.getStringExtra("userID") ?: return
        val applicationID = intent.getStringExtra("applicationID") ?: return
        val name = intent.getStringExtra("name") ?: return
        val email = intent.getStringExtra("email") ?: return
        val contact = intent.getStringExtra("contact") ?: return
        val address = intent.getStringExtra("address") ?: return
        val resume = intent.getStringExtra("resume") ?: return

        // Initialize views
        nameTextView = findViewById(R.id.applicantFullName)
        addressTextView = findViewById(R.id.applicantAddress)
        contactDetailTextView = findViewById(R.id.applicantContactDetail)
        emailAddressTextView = findViewById(R.id.applicantEmailAddress)
        reasonEditText = findViewById(R.id.reasonEditText)
        viewResumeButton = findViewById(R.id.opencv)
        acceptButton = findViewById(R.id.btn_accept)
        rejectButton = findViewById(R.id.btn_reject)

        // Populate TextViews with data
        nameTextView.text = name
        addressTextView.text = address
        contactDetailTextView.text = contact
        emailAddressTextView.text = email

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("Applications")

        // Handle View Resume button click
        viewResumeButton.setOnClickListener {
            viewResume(resume)
        }

        // Handle Accept button click
        acceptButton.setOnClickListener {
            handleApplicationStatus(userID, applicationID, "Accepted")
        }

        // Handle Reject button click
        rejectButton.setOnClickListener {
            handleApplicationStatus(userID, applicationID, "Rejected")
        }
    }

    private fun handleApplicationStatus(userID: String, applicationID: String, status: String) {
        val message = reasonEditText.text.toString()
        val applicationRef = database.child(userID).child(applicationID)

        applicationRef.child("status").setValue(status)
            .addOnSuccessListener {
                if (message.isNotEmpty()) {
                    applicationRef.child("message").setValue(message)
                }
                Toast.makeText(this, "Application $status successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to $status application: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        finish()
    }

    private fun viewResume(resumeUrl: String?) {
        if (resumeUrl != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(resumeUrl), "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No PDF viewer app found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No resume available.", Toast.LENGTH_SHORT).show()
        }
    }
}
