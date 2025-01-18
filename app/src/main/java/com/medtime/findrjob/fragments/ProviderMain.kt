package com.medtime.findrjob

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.model.JobPost
import com.medtime.findrjob.adapters.JobPostAdapter
import kotlin.collections.ArrayList

class ProviderMain : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobPostAdapter
    private val jobList = ArrayList<JobPost>()
    private lateinit var jobsDatabase: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_job_provider_dashboard, container, false)

        // Initialize Firebase reference
        jobsDatabase = FirebaseDatabase.getInstance().getReference("Job Post")

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerViewJobApplications)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = JobPostAdapter(jobList, requireContext(), jobsDatabase)
        recyclerView.adapter = adapter

        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Fetch job posts for the current provider
        fetchJobPostsForProvider()

        return view
    }

    private fun fetchJobPostsForProvider() {
        val providerId = getCurrentProviderId()

        // Ensure providerId is available
        if (providerId.isEmpty()) {
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Unable to fetch jobs. Please log in again.", Toast.LENGTH_SHORT).show()
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
                        it.jobId = snapshot.key.toString()
                        jobList.add(it) // Add the job post to the list
                    }
                }

                if (jobList.isEmpty()) {
                    Toast.makeText(requireContext(), "No jobs found.", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged() // Notify the adapter that data has changed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar.visibility = View.GONE
                Log.e("FirebaseError", databaseError.message)
                Toast.makeText(requireContext(), "Failed to load jobs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentProviderId(): String {
        // Get the providerId (user ID) from Firebase Authentication
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the Firebase listener to avoid memory leaks
        jobsDatabase.removeEventListener(valueEventListener)
    }
}