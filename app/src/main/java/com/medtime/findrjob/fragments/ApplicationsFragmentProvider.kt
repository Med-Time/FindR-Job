package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.ApplicantsDetail
import com.medtime.findrjob.R
import com.medtime.findrjob.adapters.ViewApplicationProviderAdapter
import com.medtime.findrjob.model.ApplicationData

class ApplicationsFragmentProvider : Fragment() {
    private lateinit var applicationsRecyclerView: RecyclerView
    private lateinit var applicationAdapter: ViewApplicationProviderAdapter
    private val applicationList = mutableListOf<ApplicationData>()
    private lateinit var databaseApplications: DatabaseReference
    private lateinit var databaseJobPosts: DatabaseReference
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_job_provider_dashboard, container, false)
        toolbar = view.findViewById(R.id.custom_toolbar)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Your Jobs"

        // Initialize views
        applicationsRecyclerView = view.findViewById(R.id.recyclerViewJobApplications)
        emptyView = view.findViewById(R.id.empty_view1)
        progressBar = view.findViewById(R.id.progressBar)

        // Setup RecyclerView
        applicationsRecyclerView.layoutManager = LinearLayoutManager(context)
        applicationAdapter = ViewApplicationProviderAdapter(applicationList) { application ->
            onApplicationClick(application) // Updated click listener
        }
        applicationsRecyclerView.adapter = applicationAdapter

        // Initialize Firebase references
        databaseApplications = FirebaseDatabase.getInstance().getReference("Applications")
        databaseJobPosts = FirebaseDatabase.getInstance().getReference("Job Post")

        loadApplicationsForProvider()
        return view
    }

    private fun loadApplicationsForProvider() {
        val providerId = FirebaseAuth.getInstance().currentUser?.uid
        if (providerId == null) {
            displayEmptyState("Please log in to view your applications.")
            return
        }

        progressBar.visibility = View.VISIBLE
        applicationsRecyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        databaseJobPosts.child(providerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(jobPostSnapshot: DataSnapshot) {
                    if (!jobPostSnapshot.exists()) {
                        displayEmptyState("No applications found.")
                        return
                    }

                    val jobIds = jobPostSnapshot.children.mapNotNull { it.key }
                    fetchApplicationsForJobIds(jobIds)
                }

                override fun onCancelled(error: DatabaseError) {
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

                for (userSnapshot in applicationSnapshot.children) {
                    val userID = userSnapshot.key?: continue
                    for (application in userSnapshot.children) {
                        val app = application.getValue(ApplicationData::class.java)
                        app?.let {
                            it.applicationId = application.key
                            it.userID = userID
                            if (jobIds.contains(it.jobId) && it.status == "Pending") {
                                applicationList.add(it)
                            }
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

    private fun onApplicationClick(application: ApplicationData) {
        val intent = Intent(requireContext(), ApplicantsDetail::class.java).apply {
            putExtra("userID", application.userID) // Pass userID (Seeker's ID)
            putExtra("applicationID", application.applicationId) // Pass applicationID
            putExtra("name", application.name)
            putExtra("email", application.email)
            putExtra("contact", application.contact)
            putExtra("address", application.address)
            putExtra("resume", application.fileUrl)
        }
        startActivity(intent)
    }
    private fun filterApplications(status: String?) {
        val filteredList = if (status == null || status == "All") {
            applicationList
        } else {
            applicationList.filter { it.status == status }
        }
        applicationAdapter.updateList(filteredList)
        applicationAdapter.notifyDataSetChanged()
    }

}
