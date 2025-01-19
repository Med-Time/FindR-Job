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
import com.medtime.findrjob.model.ApplicationData

class ApplicantsDetail : BaseActivity() {

    private lateinit var application: ApplicationData
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
        setContentView(R.layout.activity_applicants_detail)  // Ensure this matches your layout XML
        setupToolbar()
        supportActionBar?.title = "Job Details"
        setSearchQueryListener {  }

        val applicantId = intent.getStringExtra("applicantId") ?: return
        val jobId = intent.getStringExtra("jobId") ?: return
        val jobTitle = intent.getStringExtra("jobTitle") ?: return
        val name = intent.getStringExtra("name") ?: return
        val email = intent.getStringExtra("email") ?: return
        val contact = intent.getStringExtra("contact") ?: return
        val address = intent.getStringExtra("address") ?:return
        val resume = intent.getStringExtra("resume") ?: return

        // Initialize views


        findViewById<TextView>(R.id.fullName).text = name
        findViewById<TextView>(R.id.address).text= address
        findViewById<TextView>(R.id.contactDetail).text = contact
        findViewById<TextView>(R.id.emailAddress).text = email
        findViewById<TextView>(R.id.opencv).text = resume

        // Get data from the Intent
//        nameTextView = findViewById(R.id.fullName)
//        addressTextView = findViewById(R.id.address)
//        contactDetailTextView = findViewById(R.id.contactDetail)
//        emailAddressTextView = findViewById(R.id.emailAddress)
//        reasonEditText = findViewById(R.id.reasonEditText)
//        acceptButton = findViewById(R.id.btn_accept)
//        rejectButton = findViewById(R.id.btn_reject)
//        viewResumeButton = findViewById(R.id.opencv)
//
//        // Populate the TextViews with the applicant data
//        nameTextView.text = applicantId // You may want to replace this with the actual applicant name
//        addressTextView.text = address
//        contactDetailTextView.text = contact
//        emailAddressTextView.text = email

        // Set up Firebase
        database = FirebaseDatabase.getInstance().getReference("Applications")

        // Handle View Resume button click
        viewResumeButton.setOnClickListener {
            // Replace this with your actual file URL
            val fileUrl = "" // Get from your data model or database
            if (fileUrl.isNotEmpty()) {
                openPdf(fileUrl)
            } else {
                Toast.makeText(this, "No resume available.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Accept button click
        acceptButton.setOnClickListener {
            // Logic to accept the application
            database.child(jobId).child(applicantId).child("status").setValue("accepted")
                .addOnSuccessListener {
                    val message = reasonEditText.text.toString()
                    if (message.isNotEmpty()) {
                        database.child(jobId).child(applicantId).child("message").setValue(message)
                    }
                    Toast.makeText(this, "Application accepted.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to accept application: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle Reject button click
        rejectButton.setOnClickListener {
            // Logic to reject the application
            database.child(jobId).child(applicantId).child("status").setValue("rejected")
                .addOnSuccessListener {
                    val message = reasonEditText.text.toString()
                    if (message.isNotEmpty()) {
                        database.child(jobId).child(applicantId).child("message").setValue(message)
                    }
                    Toast.makeText(this, "Application rejected.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to reject application: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openPdf(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))
    }
}