package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.medtime.findrjob.Model.Data

class JobSeeker : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAllJobPost: DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<Data, AllJobPostViewHolder>
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_seeker)

        toolbar = findViewById(R.id.alljobpostToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Job Post"

        searchView = findViewById(R.id.searchView)

        recyclerView = findViewById(R.id.recyclerAllJob)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        // Add DividerItemDecoration
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        mAllJobPost = FirebaseDatabase.getInstance().reference.child("Public database")
        mAllJobPost.keepSynced(true)

        setupAdapter("")

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
        val firebaseSearchQuery: Query = mAllJobPost.orderByChild("title")
            .startAt(query)
            .endAt(query + "\uf8ff")

        val options = FirebaseRecyclerOptions.Builder<Data>()
            .setQuery(firebaseSearchQuery, Data::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Data, AllJobPostViewHolder>(options) {
            override fun onBindViewHolder(
                holder: AllJobPostViewHolder, position: Int, model: Data
            ) {
                holder.setJobTitle(model.title)
                holder.setJobDescription(model.description)
                holder.setJobSkills(model.skills)
                holder.setJobSalary(model.salary)

                holder.btnjobapply.setOnClickListener {
                    val intent = Intent(this@JobSeeker, JobApplicationActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllJobPostViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.alljobpost, parent, false)
                return AllJobPostViewHolder(view)
            }
        }

        recyclerView.adapter = adapter
        adapter.startListening()
    }

    class AllJobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnjobapply: Button = itemView.findViewById(R.id.allJobPostApllyButton)
        private val mView: View = itemView

        fun setJobTitle(title: String) {
            val mTitle: TextView = mView.findViewById(R.id.alljobTitle)
            mTitle.text = title
        }

        fun setJobDescription(description: String) {
            val mDescription: TextView = mView.findViewById(R.id.alljobDescription)
            mDescription.text = description
        }

        fun setJobSkills(skills: String) {
            val mSkills: TextView = mView.findViewById(R.id.alljobSkills)
            mSkills.text = skills
        }

        fun setJobSalary(salary: String) {
            val mSalary: TextView = mView.findViewById(R.id.alljobSalary)
            mSalary.text = salary
        }
    }
}
