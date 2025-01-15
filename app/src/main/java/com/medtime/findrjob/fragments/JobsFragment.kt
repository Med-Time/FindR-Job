package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.R
import com.medtime.findrjob.UserLogin
import com.medtime.findrjob.adapters.JobAdapter
import com.medtime.findrjob.model.Job

class JobsFragment : Fragment() {
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var jobAdapter: JobAdapter
    private val jobList = mutableListOf<Job>()
    private lateinit var databaseReference: DatabaseReference

    private lateinit var toolbar: Toolbar
    private lateinit var logout: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jobs, container, false)

        // Initialize toolbar
        toolbar = view.findViewById(R.id.custom_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Available Jobs"

        // Find logout button within the custom toolbar
        logout = view.findViewById(R.id.logoutButton)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val logoutIntent = Intent(requireContext(), UserLogin::class.java)
            startActivity(logoutIntent)
            requireActivity().finish()
        }

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Job Post")

        // Initialize RecyclerView, ProgressBar, and adapter
        jobsRecyclerView = view.findViewById(R.id.jobs_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        jobsRecyclerView.layoutManager = LinearLayoutManager(context)
        jobAdapter = JobAdapter(jobList)
        jobsRecyclerView.adapter = jobAdapter

        // Load jobs from Firebase
        loadJobsFromFirebase()

        return view
    }


    private fun loadJobsFromFirebase() {
        // Show progress bar while data is loading
        progressBar.visibility = View.VISIBLE
        jobsRecyclerView.visibility = View.GONE

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jobList.clear() // Clear previous data

                // Loop through all provider nodes
                for (providerSnapshot in snapshot.children) {
                    // For each provider, loop through their jobs
                    for (jobSnapshot in providerSnapshot.children) {
                        // Get the job details from Firebase
                        val job = jobSnapshot.getValue(Job::class.java)

                        // If job data exists, add it to the job list
                        job?.let { jobList.add(it) }
                    }
                }

                // Update adapter and toggle visibility
                jobAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
                jobsRecyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                // Hide progress bar and show error message
                progressBar.visibility = View.GONE
                jobsRecyclerView.visibility = View.GONE
                Toast.makeText(context, "Failed to load jobs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
