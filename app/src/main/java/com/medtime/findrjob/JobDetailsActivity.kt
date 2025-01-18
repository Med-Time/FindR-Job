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

class JobDetailsActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences

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

        // Get job details from intent
        val jobId = intent.getStringExtra("jobId")
        val jobTitle = intent.getStringExtra("jobTitle")
        val companyName = intent.getStringExtra("companyName")
        val jobDescription = intent.getStringExtra("jobDescription")
        val jobDate = intent.getStringExtra("jobDate")
        val jobSkills = intent.getStringExtra("jobSkills")
        val jobSalary = intent.getStringExtra("jobSalary")


        Log.d("JobDetailsActivity", "Job details: $jobId, $jobTitle, $companyName, $jobDescription, $jobDate, $jobSkills, $jobSalary")

        // Initialize views
        findViewById<TextView>(R.id.job_title).text = jobTitle
        findViewById<TextView>(R.id.job_company).text = companyName
        findViewById<TextView>(R.id.job_description).text = jobDescription
        findViewById<TextView>(R.id.job_date).text = jobDate
        findViewById<TextView>(R.id.job_skills).text = jobSkills
        findViewById<TextView>(R.id.job_salary).text = jobSalary

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("IgnoredJobs", 0)

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
            jobId?.let {
                val ignoredJobs = sharedPreferences.getStringSet("ignoredJobs", mutableSetOf())!!
                ignoredJobs.add(it)
                sharedPreferences.edit().putStringSet("ignoredJobs", ignoredJobs).apply()
                Toast.makeText(this, "Job ignored", Toast.LENGTH_SHORT).show()
                finish() // Close the details view
            }
        }
    }
}
