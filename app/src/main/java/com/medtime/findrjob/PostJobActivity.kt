package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.model.JobPostData
import java.text.DateFormat
import java.util.Date

class PostJobActivity : BaseActivity() {

    private lateinit var jobTitle: EditText
    private lateinit var jobDescription: EditText
    private lateinit var jobSkills: EditText
    private lateinit var jobSalary: EditText
    private lateinit var postJob: Button

    private lateinit var mJobPost: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_job)

        setupEdgeInsets()

        // Initialize Firebase
        val uId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser!!.uid
        mJobPost = FirebaseDatabase.getInstance().getReference("Job Post").child(uId)

        // Initialize Views
        jobTitle = findViewById(R.id.jobTitle)
        jobDescription = findViewById(R.id.jobDescription)
        jobSkills = findViewById(R.id.jobskill)
        jobSalary = findViewById(R.id.jobSalary)
        postJob = findViewById(R.id.btnPostJob)

        // Handle Post Job Button Click
        postJob.setOnClickListener {
            val title = jobTitle.text.toString().trim()
            val description = jobDescription.text.toString().trim()
            val skills = jobSkills.text.toString().trim()
            val salary = jobSalary.text.toString().trim()

            when {
                title.isEmpty() -> {
                    jobTitle.error = "Please Enter Job Title"
                    jobTitle.requestFocus()
                }
                description.isEmpty() -> {
                    jobDescription.error = "Please Enter Job Description"
                    jobDescription.requestFocus()
                }
                skills.isEmpty() -> {
                    jobSkills.error = "Please Enter Job Skills"
                    jobSkills.requestFocus()
                }
                salary.isEmpty() -> {
                    jobSalary.error = "Please Enter Job Salary"
                    jobSalary.requestFocus()
                }
                else -> {
                    val id = mJobPost.push().key ?: ""
                    val date = DateFormat.getDateInstance().format(Date())

                    val jobPostData = JobPostData(title, description, skills, salary, date)
                    mJobPost.child(id).setValue(jobPostData)

                    Toast.makeText(this, "Successfully Posted", Toast.LENGTH_SHORT).show()
                    finishActivity()
                }
            }
        }
    }

    private fun finishActivity() {
        val intent = Intent(this, JobProviderDashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
