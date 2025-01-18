package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.R
import com.medtime.findrjob.model.JobPostData
import java.text.DateFormat
import java.util.Date

class JobsFragmentProvider : Fragment() {

    private lateinit var jobTitle: EditText
    private lateinit var jobDescription: EditText
    private lateinit var jobSkills: EditText
    private lateinit var jobSalary: EditText
    private lateinit var postJob: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mJobPost: DatabaseReference
    private lateinit var mPublicDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_insertdata_job_provider, container, false)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        val mUser = firebaseAuth.currentUser
        val uId = mUser?.uid.orEmpty()

        mJobPost = FirebaseDatabase.getInstance().getReference("Job Post").child(uId)
        mPublicDatabase = FirebaseDatabase.getInstance().getReference("Public database")

        // Initialize Views
        jobTitle = view.findViewById(R.id.jobTitle)
        jobDescription = view.findViewById(R.id.jobDescription)
        jobSkills = view.findViewById(R.id.jobskill)
        jobSalary = view.findViewById(R.id.jobSalary)
        postJob = view.findViewById(R.id.btnPostJob)

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

                    val jobPostData = JobPostData(title, description, skills, salary, id, date)
                    mJobPost.child(id).setValue(jobPostData)
                    mPublicDatabase.child(id).setValue(jobPostData)

                    Toast.makeText(requireContext(), "Successfully Posted", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
