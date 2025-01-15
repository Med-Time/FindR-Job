package com.medtime.findrjob

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.medtime.findrjob.model.ApplicationData
import kotlin.collections.ArrayList

class JobProviderDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobApplicationAdapter
    private val applicationList = ArrayList<ApplicationData>()
    private lateinit var applicationsDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_provider_dashboard)

        recyclerView = findViewById(R.id.recyclerViewJobApplications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JobApplicationAdapter(applicationList, this)
        recyclerView.adapter = adapter

        applicationsDatabase = FirebaseDatabase.getInstance().getReference("Applications")

        fetchJobApplications()
    }

    private fun fetchJobApplications() {
        applicationsDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                applicationList.clear()
                for (snapshot in dataSnapshot.children) {
                    val application = snapshot.getValue(ApplicationData::class.java)
                    application?.let { applicationList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}
