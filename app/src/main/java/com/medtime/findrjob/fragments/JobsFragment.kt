package com.medtime.findrjob.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.BaseActivity
import com.medtime.findrjob.JobDetailsActivity
import com.medtime.findrjob.R
import com.medtime.findrjob.adapters.JobAdapter
import com.medtime.findrjob.model.Job

class JobsFragment : Fragment() {

    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var jobAdapter: JobAdapter
    private val jobList = mutableListOf<Job>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var toolbar: Toolbar
    private lateinit var ignoredJobsReference: DatabaseReference
    private lateinit var appliedJobsReference: DatabaseReference

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

        // Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("Job Post")
        appliedJobsReference = FirebaseDatabase.getInstance().getReference("Applications")
        ignoredJobsReference = FirebaseDatabase.getInstance().getReference("IgnoredJobs")

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


    private fun loadJobsFromFirebase() {
        progressBar.visibility = View.VISIBLE
        jobsRecyclerView.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Please log in to view jobs.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        ignoredJobsReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(ignoredSnapshot: DataSnapshot) {
                val ignoredJobIds = mutableSetOf<String>()
                for (ignoredJob in ignoredSnapshot.children) {
                    ignoredJob.key?.let { ignoredJobIds.add(it) }
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
                                val providerRef = FirebaseDatabase.getInstance().getReference("Providers")

                                for (providerSnapshot in snapshot.children) {
                                    val providerID = providerSnapshot.key.toString()

                                    providerRef.child(providerID).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(providerData: DataSnapshot) {
                                            val companyName = providerData.child("companyName").value.toString()

                                            for (jobSnapshot in providerSnapshot.children) {
                                                val job = jobSnapshot.getValue(Job::class.java)
                                                job!!.id = jobSnapshot.key.toString()
                                                job.company = companyName

                                                // Exclude ignored or applied jobs
                                                if (!ignoredJobIds.contains(job.id) && !appliedJobIds.contains(job.id)) {
                                                    jobList.add(job)
                                                }
                                            }
                                            jobAdapter.updateList(jobList)
                                            progressBar.visibility = View.GONE
                                            jobsRecyclerView.visibility = View.VISIBLE
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(context, "Failed to load provider details: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
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

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to load ignored jobs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun filterJobs(query: String) {
        val filteredList = jobList.filter { job ->
            job.title.contains(query, ignoreCase = true) ||
                    job.company.contains(query, ignoreCase = true) ||
                    job.skills.contains(query, ignoreCase = true)
        }
        jobAdapter.updateList(filteredList)
    }
}
