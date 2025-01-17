package com.medtime.findrjob

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.model.JobPost
import kotlin.collections.ArrayList

class JobProviderDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobPostAdapter
    private val jobList = ArrayList<JobPost>()
    private lateinit var jobsDatabase: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_provider_dashboard)
        jobsDatabase = FirebaseDatabase.getInstance().getReference("Job Post")
        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerViewJobApplications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JobPostAdapter(jobList, this,jobsDatabase)
        recyclerView.adapter = adapter

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Initialize Firebase reference
//        jobsDatabase = FirebaseDatabase.getInstance().getReference("Job Post")

        // Fetch job posts for the current provider
        fetchJobPostsForProvider()
    }

    private fun fetchJobPostsForProvider() {
        val providerId = getCurrentProviderId()

        // Ensure providerId is available
        if (providerId.isEmpty()) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Unable to fetch jobs. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Query Firebase for jobs by providerId
        valueEventListener = jobsDatabase.child(providerId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                jobList.clear() // Clear the previous list before adding new data

                for (snapshot in dataSnapshot.children) {
                    val jobPost = snapshot.getValue(JobPost::class.java)
                    jobPost?.let {
                        it.jobId= snapshot.key.toString()
                        jobList.add(it) } // Add the job post to the list
                }

                if (jobList.isEmpty()) {
                    Toast.makeText(this@JobProviderDashboardActivity, "No jobs found.", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged() // Notify the adapter that data has changed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar.visibility = View.GONE
                Log.e("FirebaseError", databaseError.message)
                Toast.makeText(this@JobProviderDashboardActivity, "Failed to load jobs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentProviderId(): String {
        // Get the providerId (user ID) from Firebase Authentication
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the Firebase listener to avoid memory leaks
        jobsDatabase.removeEventListener(valueEventListener)
    }
}
