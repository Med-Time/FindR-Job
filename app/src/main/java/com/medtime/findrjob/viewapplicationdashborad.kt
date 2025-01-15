package com.medtime.findrjob

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class viewapplicationdashborad : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobApplicationAdapter
    private val jobApplications = ArrayList<JobApplication>()
    private lateinit var progressBar: ProgressBar
    private lateinit var jobsDatabase: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewapplicationdashborad)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewJobApplications1)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JobApplicationAdapter(jobApplications,this)
        recyclerView.adapter = adapter

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Initialize Firebase reference
        jobsDatabase = FirebaseDatabase.getInstance().getReference("Applications")

        // Fetch data from Firebase
        fetchJobApplications()
    }

    private fun fetchJobApplications() {
        val providerId = getCurrentProviderId()
//        val applicantId=

        // Ensure providerId is available
        if (providerId.isEmpty()) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Unable to fetch jobs. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Query Firebase for jobs by providerId

        var valueEventListener =
            jobsDatabase.child(providerId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE
                    jobApplications.clear() // Clear the previous list before adding new data

                    for (snapshot in dataSnapshot.children) {
                        val jobPost = snapshot.getValue(JobApplication::class.java)
                        jobPost?.let {
                            it.applicantId = snapshot.key.toString()
                            jobApplications.add(it)
                        } // Add the job post to the list
                    }

                    if (jobApplications.isEmpty()) {
                        Toast.makeText(
                            this@viewapplicationdashborad,
                            "No applicant found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    adapter.notifyDataSetChanged() // Notify the adapter that data has changed
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Log.e("FirebaseError", databaseError.message)
                    Toast.makeText(
                        this@viewapplicationdashborad,
                        "Failed to load applicants",
                        Toast.LENGTH_SHORT
                    ).show()
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

