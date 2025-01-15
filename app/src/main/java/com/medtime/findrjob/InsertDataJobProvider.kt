package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.model.JobPostData
import java.text.DateFormat
import java.util.Date

class InsertDataJobProvider : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var jobTitle: EditText
    private lateinit var jobDescription: EditText
    private lateinit var jobSkills: EditText
    private lateinit var jobSalary: EditText
    private lateinit var postJob: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mJobPost: DatabaseReference
    private lateinit var mPublicDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertdata_job_provider)

        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FindR Job Platform - Post Your Job"

        firebaseAuth = FirebaseAuth.getInstance()
        val mUser = firebaseAuth.currentUser
        val uId = mUser?.uid.orEmpty()

        mJobPost = FirebaseDatabase.getInstance().getReference("Job Post").child(uId)
        mPublicDatabase = FirebaseDatabase.getInstance().getReference("Public database")

        jobTitle = findViewById(R.id.jobTitle)
        jobDescription = findViewById(R.id.jobDescription)
        jobSkills = findViewById(R.id.jobskill)
        jobSalary = findViewById(R.id.jobSalary)
        postJob = findViewById(R.id.btnPostJob)

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
                salary.isEmpty() -> {
                    jobSalary.error = "Please Enter Job Salary"
                    jobSalary.requestFocus()
                }
                skills.isEmpty() -> {
                    jobSkills.error = "Please Enter Job Skills"
                    jobSkills.requestFocus()
                }
                else -> {
                    val id = mJobPost.push().key ?: ""
                    val date = DateFormat.getDateInstance().format(Date())

                    val jobPostData = JobPostData(title, description, skills, salary, id, date)
                    mJobPost.child(id).setValue(jobPostData)
                    mPublicDatabase.child(id).setValue(jobPostData)

                    Toast.makeText(this, "Successfully Posted", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, JobProvider::class.java))
                }
            }
        }
    }
}
