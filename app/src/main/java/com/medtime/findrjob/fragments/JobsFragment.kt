package com.medtime.findrjob.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.AboutUs
import com.medtime.findrjob.JobDetailsActivity
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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appliedJobsReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enable options menu in fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jobs, container, false)
        toolbar = view.findViewById(R.id.custom_toolbar)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Available Jobs"

        // Initialize RecyclerView and ProgressBar
        jobsRecyclerView = view.findViewById(R.id.jobs_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        jobsRecyclerView.layoutManager = LinearLayoutManager(context)
        jobAdapter = JobAdapter(jobList.toMutableList()) { job ->
            // Navigate to JobDetailsActivity when a job is clicked
            val intent = Intent(requireContext(), JobDetailsActivity::class.java).apply {
                putExtra("jobId", job.id)
                putExtra("jobTitle", job.title)
                putExtra("companyName", job.company)
                putExtra("jobDescription", job.description)
                putExtra("jobDate", job.date)
                putExtra("jobSkills", job.skills)
                putExtra("jobSalary", job.salary)
            }
            startActivity(intent)
        }
        jobsRecyclerView.adapter = jobAdapter

        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Job Post")
        appliedJobsReference = FirebaseDatabase.getInstance().getReference("Applications")

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("IgnoredJobs", 0)

        // Load jobs from Firebase
        loadJobsFromFirebase()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
        val searchItem = menu.findItem(R.id.search_view)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterJobs(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterJobs(it) }
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun loadJobsFromFirebase() {
        progressBar.visibility = View.VISIBLE
        jobsRecyclerView.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please log in to view jobs.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        appliedJobsReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(appliedSnapshot: DataSnapshot) {
                val appliedJobIds = mutableSetOf<String>()
                for (application in appliedSnapshot.children) {
                    val jobId = application.child("jobId").value.toString()
                    appliedJobIds.add(jobId)
                }

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        jobList.clear()
                        for (providerSnapshot in snapshot.children) {
                            for (jobSnapshot in providerSnapshot.children) {
                                val job = jobSnapshot.getValue(Job::class.java)
                                job?.let {
                                    // Check if the job is ignored or applied
                                    val ignoredJobs = sharedPreferences.getStringSet("ignoredJobs", emptySet())
                                    if (!ignoredJobs!!.contains(job.id) && !appliedJobIds.contains(job.id)) {
                                        jobList.add(job)
                                    }
                                }
                            }
                        }
                        jobAdapter.updateList(jobList) // Notify adapter with the updated job list
                        progressBar.visibility = View.GONE
                        jobsRecyclerView.visibility = View.VISIBLE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to load jobs: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load applied jobs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterJobs(query: String) {
        val filteredList = jobList.filter { job ->
            job.title.contains(query, ignoreCase = true) || // Search by job title
                    job.company.contains(query, ignoreCase = true)  || // Optionally search by company name
                    job.skills.contains(query, ignoreCase = true) // Optionally search by skills
        }

        jobAdapter.updateList(filteredList) // Update the adapter with the filtered list
    }
}
