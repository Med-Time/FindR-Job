package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.model.JobPostData

class JobProvider : AppCompatActivity() {
    private lateinit var btnfloat: FloatingActionButton
    private lateinit var viewapplications: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var JobPostDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       enableEdgeToEdge()
        setContentView(R.layout.activity_job_provider)

        btnfloat = findViewById(R.id.fab)
        viewapplications = findViewById(R.id.button_view_applications)

        firebaseAuth = FirebaseAuth.getInstance()
        val mUser: FirebaseUser? = firebaseAuth.currentUser
        val uId = mUser?.uid ?: ""

        JobPostDatabase = FirebaseDatabase.getInstance().reference.child("Job Post").child(uId)
        recyclerView = findViewById(R.id.recycler_job_post_id)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = linearLayoutManager

        viewapplications.setOnClickListener {
            val intent = Intent(this@JobProvider, JobProviderDashboardActivity::class.java)
            startActivity(intent)
        }

        btnfloat.setOnClickListener {
            val floatIntent = Intent(this@JobProvider, InsertDataJobProvider::class.java)
            startActivity(floatIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<JobPostData>()
            .setQuery(JobPostDatabase, JobPostData::class.java)
            .build()
        val adapter = object : FirebaseRecyclerAdapter<JobPostData, MyViewHolder>(options) {
            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: JobPostData) {
                holder.setJobTitle(model.title)
                holder.setJobDate(model.date)
                holder.setJobDescription(model.description)
                holder.setJobSkills(model.skills)
                holder.setJobSalary(model.salary)

                holder.btnEdit.setOnClickListener {
                    // Implement edit functionality
                    val intent = Intent(this@JobProvider, EditJobPostActivity::class.java)
                    intent.putExtra("postId", getRef(position).key)
                    intent.putExtra("title", model.title)
                    intent.putExtra("date", model.date)
                    intent.putExtra("description", model.description)
                    intent.putExtra("skills", model.skills)
                    intent.putExtra("salary", model.salary)
                    startActivity(intent)
                }

                holder.btnDelete.setOnClickListener {
                    // Implement delete functionality
                    AlertDialog.Builder(this@JobProvider)
                        .setTitle("Delete Job Post")
                        .setMessage("Are you sure you want to delete this job post?")
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            getRef(position).removeValue()
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.job_post_item, parent, false)
                return MyViewHolder(view)
            }
        }
        adapter.startListening()
        recyclerView.adapter = adapter
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myview: View = itemView
        val btnEdit: Button = myview.findViewById(R.id.btnEdit)
        val btnDelete: Button = myview.findViewById(R.id.btnDelete)
        val btnSave: Button = myview.findViewById(R.id.btnSaveJob)

        fun setJobTitle(title: String) {
            val mTitle: TextView = myview.findViewById(R.id.jobTitle)
            mTitle.text = title
        }

        fun setJobDate(date: String) {
            val mDate: TextView = myview.findViewById(R.id.jobDate)
            mDate.text = date
        }

        fun setJobDescription(description: String) {
            val mDescription: TextView = myview.findViewById(R.id.jobDescription)
            mDescription.text = description
        }

        fun setJobSkills(skills: String) {
            val mSkills: TextView = myview.findViewById(R.id.jobSkills)
            mSkills.text = skills
        }

        fun setJobSalary(salary: String) {
            val mSalary: TextView = myview.findViewById(R.id.jobSalary)
            mSalary.text = salary
        }
    }
}
