package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.model.JobPostData
class newjobprovider : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var jobPostRef: DatabaseReference
    private lateinit var providerRef: DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<JobPostData, AllJobPostViewHolder>
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newjobprovider)

        // Initialize the toolbar
        toolbar = findViewById(R.id.alljobpostToolbarprovider)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Job Posts"

        // Initialize the search view
        searchView = findViewById(R.id.searchViewprovider)

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerAllJobprovider)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Add DividerItemDecoration
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // Initialize Firebase database references
        jobPostRef = FirebaseDatabase.getInstance().reference.child("Job Post")
        providerRef = FirebaseDatabase.getInstance().reference.child("Providers") // Provider companyName data

        jobPostRef.keepSynced(true)

        // Set up the adapter with an empty search query initially
        setupAdapter("")

        // Set up search query listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                setupAdapter(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                setupAdapter(newText ?: "")
                return false
            }
        })
    }

    private fun setupAdapter(query: String) {
        // Create a Firebase query based on the search text
        val firebaseSearchQuery: Query = jobPostRef.orderByChild("title")
            .startAt(query)
            .endAt(query + "\uf8ff")

        val options = FirebaseRecyclerOptions.Builder<JobPostData>()
            .setQuery(firebaseSearchQuery, JobPostData::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<JobPostData, AllJobPostViewHolder>(options) {
            override fun onBindViewHolder(
                holder: AllJobPostViewHolder, position: Int, model: JobPostData
            ) {
                holder.setJobTitle(model.title)
                holder.setJobDescription(model.description)
                holder.setJobSkills(model.skills)
                holder.setJobSalary(model.salary)

                // Fetch provider companyName using providerId
                providerRef.child(model.providerId).child("companyName").addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val companyName = snapshot.getValue(String::class.java) ?: "Name not available"
                            holder.setProviderLocation(companyName)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            holder.setProviderLocation("Error fetching Name")
                        }
                    }
                )

                holder.btnedit.setOnClickListener {
                    // Implement your logic here for applying to a job
                    // For example, navigating to JobApplicationActivity
                    val intent = Intent(this@newjobprovider, EditJobPostActivity::class.java)
                    startActivity(intent)
                }

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllJobPostViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.activity_edit_job_item, parent, false)
                return AllJobPostViewHolder(view)
            }
        }

        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening() // Start listening when the activity starts
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening() // Stop listening when the activity stops to prevent memory leaks
    }

    class AllJobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnedit: Button = itemView.findViewById(R.id.btnEdit)
        val btndelete: Button = itemView.findViewById(R.id.btnDelete)

        private val mView: View = itemView

        fun setJobTitle(title: String) {
            val mTitle: TextView = mView.findViewById(R.id.jobTitle)
            mTitle.text = title
        }

        fun setJobDescription(description: String) {
            val mDescription: TextView = mView.findViewById(R.id.jobDescription)
            mDescription.text = description
        }

        fun setJobSkills(skills: String) {
            val mSkills: TextView = mView.findViewById(R.id.jobSkills)
            mSkills.text = skills
        }

        fun setJobSalary(salary: String) {
            val mSalary: TextView = mView.findViewById(R.id.jobSalary)
            mSalary.text = salary
        }

        fun setProviderLocation(companyName: String) {
            val mLocation: TextView = mView.findViewById(R.id.job_company)
            mLocation.text = companyName
        }
    }

}


