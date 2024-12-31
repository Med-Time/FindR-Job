package com.medtime.findrjob

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*
import com.medtime.findrjob.Model.Data

class EditJobPostActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var jobTitle: EditText
    private lateinit var jobDescription: EditText
    private lateinit var jobSkills: EditText
    private lateinit var jobSalary: EditText
    private lateinit var jobPostDatabase: DatabaseReference
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_job_post)

        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Edit Job Post"

        jobTitle = findViewById(R.id.editJobTitle)
        jobDescription = findViewById(R.id.editJobDescription)
        jobSkills = findViewById(R.id.editJobSkills)
        jobSalary = findViewById(R.id.editJobSalary)

        postId = intent.getStringExtra("postId").orEmpty()
        jobPostDatabase = FirebaseDatabase.getInstance().getReference("Job Post").child(postId)

        // Fetch and populate job details
        jobPostDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobPost = snapshot.getValue(Data::class.java)
                if (jobPost != null) {
                    jobTitle.setText(jobPost.title)
                    jobDescription.setText(jobPost.description)
                    jobSkills.setText(jobPost.skills)
                    jobSalary.setText(jobPost.salary)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditJobPostActivity, "Failed to load job post.", Toast.LENGTH_SHORT).show()
            }
        })

        // Uncomment this if you decide to include the save button functionality
//        findViewById<Button>(R.id.btnSaveJob).setOnClickListener {
//            val title = jobTitle.text.toString().trim()
//            val description = jobDescription.text.toString().trim()
//            val skills = jobSkills.text.toString().trim()
//            val salary = jobSalary.text.toString().trim()
//
//            val map = mapOf(
//                "title" to title,
//                "description" to description,
//                "skills" to skills,
//                "salary" to salary
//            )
//
//            jobPostDatabase.updateChildren(map)
//                .addOnSuccessListener {
//                    Toast.makeText(this@EditJobPostActivity, "Job post updated successfully.", Toast.LENGTH_SHORT).show()
//                    // Optionally, refresh the data and UI after update
//                    jobPostDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            val updatedPost = snapshot.getValue(Data::class.java)
//                            updatedPost?.let {
//                                jobTitle.setText(it.title)
//                                jobDescription.setText(it.description)
//                                jobSkills.setText(it.skills)
//                                jobSalary.setText(it.salary)
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            // Handle errors if needed
//                        }
//                    })
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this@EditJobPostActivity, "Failed to update job post.", Toast.LENGTH_SHORT).show()
//                }
//        }
    }
}
