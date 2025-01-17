package com.medtime.findrjob.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.R
import com.medtime.findrjob.adapters.ViewApplicationProviderAdapter
import com.medtime.findrjob.model.Application

class ApplicationsFragmentProvider : Fragment() {
    private lateinit var applicationsRecyclerView: RecyclerView
    private lateinit var applicationAdapter: ViewApplicationProviderAdapter
    private val applicationList = ArrayList<Application>()
    private lateinit var databaseApplications: DatabaseReference
    private lateinit var databaseJobPosts: DatabaseReference
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_job_provider_dashboard, container, false)

        // Initialize views
        applicationsRecyclerView = view.findViewById(R.id.recyclerViewJobApplications)
        emptyView = view.findViewById(R.id.empty_view1)
        progressBar = view.findViewById(R.id.progressBar)

        // Setup RecyclerView with LinearLayoutManager and Adapter
        applicationsRecyclerView.layoutManager = LinearLayoutManager(context)
        applicationAdapter = ViewApplicationProviderAdapter(applicationList, requireContext())
        applicationsRecyclerView.adapter = applicationAdapter

        // Initialize Firebase database references
        databaseApplications = FirebaseDatabase.getInstance().getReference("Applications")
        databaseJobPosts = FirebaseDatabase.getInstance().getReference("Job Post")

        Log.d("ApplicationsFragment", "onCreateView initialized.")
        loadApplicationsForProvider()

        return view
    }

    private fun loadApplicationsForProvider() {
        Log.d("ApplicationsFragment", "Fetching applications for provider.")
        val providerId = FirebaseAuth.getInstance().currentUser?.uid

        if (providerId == null) {
            Log.e("ApplicationsFragment", "Provider not logged in.")
            displayEmptyState("Please log in to view your applications.")
            return
        }

        // Show progress bar while fetching data
        progressBar.visibility = View.VISIBLE
        applicationsRecyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
       Log.d("Provider Id",providerId)
        // Fetch job posts created by this provider
        databaseJobPosts.orderByChild("providerID").equalTo(providerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(jobPostSnapshot: DataSnapshot) {
                    if (!jobPostSnapshot.exists()) {
                        Log.d("ApplicationsFragment", "No job posts found for this provider.")
                        displayEmptyState("No applications found.")
                        return
                    }

                    val jobIds = mutableListOf<String>()
                    for (jobSnapshot in jobPostSnapshot.children) {
                        val jobId = jobSnapshot.key
                        jobId?.let { jobIds.add(it) }
                    }

                    // Fetch applications for the retrieved job IDs
                    fetchApplicationsForJobIds(jobIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsFragment", "Error fetching job posts.", error.toException())
                    displayEmptyState("Error loading applications. Please try again later.")
                }
            })
    }

    private fun fetchApplicationsForJobIds(jobIds: List<String>) {
        if (jobIds.isEmpty()) {
            displayEmptyState("No applications found.")
            return
        }

        databaseApplications.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(applicationSnapshot: DataSnapshot) {
                applicationList.clear()

                for (userSnapshot in applicationSnapshot.children) { // Iterate through userIDs
                    for (application in userSnapshot.children) { // Iterate through applicationIDs
                        val app = application.getValue(Application::class.java)
                        if (app != null && jobIds.contains(app.jobID)) {
                            applicationList.add(app)
                        }
                    }
                }

                if (applicationList.isEmpty()) {
                    displayEmptyState("No applications found.")
                } else {
                    applicationsRecyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }

                progressBar.visibility = View.GONE
                applicationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ApplicationsFragment", "Error fetching applications.", error.toException())
                displayEmptyState("Error loading applications. Please try again later.")
            }
        })
    }

    private fun displayEmptyState(message: String) {
        emptyView.text = message
        emptyView.visibility = View.VISIBLE
        applicationsRecyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }
}
