package com.medtime.findrjob

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
        setupEdgeInsets()
        setupToolbar()
        supportActionBar?.title = "Job Details"
        setSearchQueryListener {  }

        // Get job details from intent
        val jobId = intent.getStringExtra("jobId")
        val jobTitle = intent.getStringExtra("jobTitle")
        val companyName = intent.getStringExtra("companyName")
        val logoUrl = intent.getStringExtra("logoUrl")
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
        val imageView = findViewById<ImageView>(R.id.job_company_logo)
        Glide.with(this)
            .load(logoUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)

        // Handle Apply button
        findViewById<Button>(R.id.btn_apply).setOnClickListener {
            val intent = Intent(this, JobApplicationActivity::class.java).apply {
                putExtra("jobTitle", jobTitle)
                putExtra("jobId", jobId)
                putExtra("companyName", companyName)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_ignore).setOnClickListener {
            if (jobId != null) {
                //Create a alert dialog
                val builder = android.app.AlertDialog.Builder(this)
                builder.setIcon(R.drawable.ic_danger)
                builder.setTitle("Confirm Ignore this Job")
                builder.setMessage("Are you sure ?")
                builder.setPositiveButton("Yes") { _, _ ->
                    ignoreRef.child(userID).child(jobId).setValue(true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Job ignored successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, JobSeekerDashboard::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to ignore job: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: android.app.AlertDialog = builder.create()
                dialog.show()
            } else {
                Toast.makeText(this, "Error: User or Job ID is missing!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
