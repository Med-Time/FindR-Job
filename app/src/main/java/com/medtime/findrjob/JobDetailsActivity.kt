package com.medtime.findrjob

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JobDetailsActivity : BaseActivity() {
    private lateinit var ignoreRef : DatabaseReference
    private lateinit var userID : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar()
        supportActionBar?.title = "Job Details"
        setSearchQueryListener {  }

        // Get job details from intent
        val jobId = intent.getStringExtra("jobId")
        val jobTitle = intent.getStringExtra("jobTitle")
        val companyName = intent.getStringExtra("companyName")
        val jobDescription = intent.getStringExtra("jobDescription")
        val jobDate = intent.getStringExtra("jobDate")
        val jobSkills = intent.getStringExtra("jobSkills")
        val jobSalary = intent.getStringExtra("jobSalary")

        userID = FirebaseAuth.getInstance().currentUser?.uid!!
        ignoreRef = FirebaseDatabase.getInstance().reference.child("IgnoredJobs")

        // Initialize views
        findViewById<TextView>(R.id.job_title).text = jobTitle
        findViewById<TextView>(R.id.job_company).text = companyName
        findViewById<TextView>(R.id.job_description).text = jobDescription
        findViewById<TextView>(R.id.job_date).text = "Posted on : $jobDate"
        findViewById<TextView>(R.id.job_skills).text = "Required Skills:\n$jobSkills"
        findViewById<TextView>(R.id.job_salary).text = "Salary: $jobSalary"

        // Handle Apply button
        findViewById<Button>(R.id.btn_apply).setOnClickListener {
            val intent = Intent(this, JobApplicationActivity::class.java).apply {
                putExtra("jobTitle", jobTitle)
                putExtra("jobId", jobId)
                putExtra("companyName", companyName)
            }
            startActivity(intent)
        }

        // Handle Ignore button
        findViewById<Button>(R.id.btn_ignore).setOnClickListener {
            if (jobId != null) {
                ignoreRef.child(userID).child(jobId).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Job ignored successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close the details view
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to ignore job: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error: User or Job ID is missing!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
