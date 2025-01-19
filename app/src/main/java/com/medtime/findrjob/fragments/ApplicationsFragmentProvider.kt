package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.ApplicantsDetail
import com.medtime.findrjob.BaseActivity
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

        // Setup RecyclerView with LinearLayoutManager and Adapter
        applicationsRecyclerView.layoutManager = LinearLayoutManager(context)
//        applicationAdapter = ViewApplicationProviderAdapter(applicationList, requireContext()) { application ->
//            onApplicationClick(application) // Click listener for applications
//        }
        applicationAdapter = ViewApplicationProviderAdapter(applicationList, requireContext()) { job ->
            // Navigate to JobDetailsActivity when a job is clicked
            val intent = Intent(requireContext(), ApplicantsDetail::class.java).apply {
                putExtra("jobId", job.jobId)
                putExtra("jobTitle", job.jobTitle)
                putExtra("companyName", job.company)
                putExtra("email", job.email)
                putExtra("jobDate", job.date)
                putExtra("contact", job.contact)
                putExtra("address", job.address)
                putExtra("resume", job.fileUrl)
            }
            startActivity(intent)
        }

     applicationsRecyclerView.adapter = applicationAdapter
        // Initialize Firebase database references
        databaseApplications = FirebaseDatabase.getInstance().getReference("Applications")
        databaseJobPosts = FirebaseDatabase.getInstance().getReference("Job Post")

        Log.d("ApplicationsFragment", "onCreateView initialized.")
        loadApplicationsForProvider()

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setSearchQueryListener { query ->
            filterJobs(query)
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as? BaseActivity)?.setSearchQueryListener(null)
    }

    private fun loadApplicationsForProvider() {
        Log.d("ApplicationsFragment", "Fetching applications for provider.")
        val providerId = FirebaseAuth.getInstance().currentUser?.uid

        if (providerId == null) {
            Log.e("ApplicationsFragment", "Provider not logged in.")
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
                        Log.d("ApplicationsFragment", "No job posts found for this provider.")
                        displayEmptyState("No applications found.")
                        return
                    }

                    val jobIds = mutableListOf<String>()
                    for (jobSnapshot in jobPostSnapshot.children) {
                        jobSnapshot.key?.let { jobIds.add(it) }
                    }

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

                for (userSnapshot in applicationSnapshot.children) {
                    for (application in userSnapshot.children) {
                        val app = application.getValue(ApplicationData::class.java)
                        if (app != null && jobIds.contains(app.jobId)) {
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
    private fun filterJobs(query: String) {
        val filteredList = applicationList.filter { job ->
            job.company?.contains(query, ignoreCase = true) ?: true || // Search by company
                    job.jobTitle?.contains(query, ignoreCase = true) ?: true // Search by job title
        }
        applicationAdapter.updateList(filteredList)
        applicationAdapter.notifyDataSetChanged() // Add this line
    }


    // Function to handle application clicks
    private fun onApplicationClick(application: ApplicationData) {
        // Handle the click on the application item
        // You might want to navigate to an application detail screen or show a dialog with more information
//        Log.d("ApplicationsFragment", "Application clicked: ${application.jobId}"
        Toast.makeText(requireContext(),"clicked on application id ${application.jobId}",Toast.LENGTH_SHORT).show()
        // Implement further logic, such as opening a new fragment or activity to show application details
    }

}