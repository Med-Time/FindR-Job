package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditJobItem : AppCompatActivity() {
    private lateinit var editbutton: Button
    private lateinit var delbutton: Button
    private lateinit var editjob: Button

    private lateinit var jobPostDatabase: DatabaseReference

    private lateinit var jobId: String
    private lateinit var providerId: String


    private lateinit var jobTitle: TextView
    private lateinit var jobDescription: TextView
    private lateinit var jobSalary: TextView
    private lateinit var jobSkills: TextView
    private lateinit var jobDate: TextView

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_edit_job_item)
//        val applicantId = intent.getStringExtra("applicantId")
//        editbutton.setOnClickListener()
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_job_item)
        Log.d("EditJobPostActivity", "Activity started successfully")
        // Get the applicantId or postId passed from the previous activity
//        val jobId = intent.getStringExtra("jobId")

        // Initialize buttons
        jobTitle = findViewById(R.id.jobTitle)
        jobDescription = findViewById(R.id.jobDescription)
        jobSalary = findViewById(R.id.jobSalary)
        jobSkills = findViewById(R.id.jobSkills)
        jobDate = findViewById(R.id.jobDate)
        editbutton = findViewById(R.id.btnEdit)
        delbutton = findViewById(R.id.btnDelete)


        jobTitle.text = intent.getStringExtra("jobTitle")
        jobDescription.text = intent.getStringExtra("jobDescription")
        jobSalary.text = intent.getStringExtra("jobSalary")
        jobSkills.text = intent.getStringExtra("jobSkills")
        jobDate.text = intent.getStringExtra("jobDate")
        Log.d("check","beforefirebase")
        providerId= FirebaseAuth.getInstance().currentUser?.uid.toString()
        jobId = intent.getStringExtra("jobId").orEmpty()
        jobPostDatabase = FirebaseDatabase.getInstance().getReference("Job Post").child(providerId).child(jobId)

        // Set the Edit button click listener


        editbutton.setOnClickListener {
            if (jobId.isNullOrEmpty()) {
                Toast.makeText(this, "Job ID is missing", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("EditJobItem", "Navigating to EditJobPostActivity with jobId: $jobId")
                val editbuttonIntent = Intent(this, EditJobPostActivity::class.java)
                editbuttonIntent.putExtra("jobId", jobId)
                startActivity(editbuttonIntent)
            }
        }



//        // Set the Delete button click listener
        delbutton.setOnClickListener {
            // Delete the record from Firebase using the applicantId
//            if (applicantId != null) {
//                deleteApplication(applicantId)
//            }
                    // Delete button click listener
                    delbutton.setOnClickListener {
                        // Delete the job post from the database
                        jobId.let { id ->
                            jobPostDatabase.child(id).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@EditJobItem, "Job post deleted successfully.", Toast.LENGTH_SHORT).show()
                                    finish() // Close the activity after deletion
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@EditJobItem, "Failed to delete job post.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

        }
    }
}


