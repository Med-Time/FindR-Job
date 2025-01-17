package com.medtime.findrjob.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.medtime.findrjob.R
import com.medtime.findrjob.adapters.MyApplicationAdapter
import com.medtime.findrjob.model.Application

class ApplicationsFragment : Fragment() {
    private lateinit var applicationsRecyclerView: RecyclerView
    private lateinit var applicationAdapter: MyApplicationAdapter
    private var applicationList = mutableListOf<Application>()
    private lateinit var database: DatabaseReference
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_applications, container, false)
        // Initialize toolbar
        toolbar = view.findViewById(R.id.custom_toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Your Applications"

        applicationsRecyclerView = view.findViewById(R.id.applications_recycler_view)
        emptyView = view.findViewById(R.id.empty_view) // TextView for empty state
        progressBar = view.findViewById(R.id.progress_bar) // Progress bar

        applicationsRecyclerView.layoutManager = LinearLayoutManager(context)
        // Initialize the adapter with an empty list
        applicationAdapter = MyApplicationAdapter(applicationList)
        applicationsRecyclerView.adapter = applicationAdapter

        database = FirebaseDatabase.getInstance().getReference("Applications")
        loadApplicationsFromDatabase()

        return view
    }

    private fun loadApplicationsFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ApplicationsFragment", "User not logged in.")
            emptyView.visibility = View.VISIBLE
            emptyView.text = "Please log in to view your applications."
            applicationsRecyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            return
        }

        // Show progress bar while data is being fetched
        progressBar.visibility = View.VISIBLE
        applicationsRecyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        database.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    applicationList.clear() // Clear the existing data before adding new data

                    if (snapshot.exists()) {
                        for (applicationSnapshot in snapshot.children) {
                            val application = applicationSnapshot.getValue(Application::class.java)
                            application?.let { applicationList.add(it) }
                        }

                        emptyView.visibility = View.GONE
                        applicationsRecyclerView.visibility = View.VISIBLE
                    } else {
                        emptyView.visibility = View.VISIBLE
                        emptyView.text = "No applications found."
                        applicationsRecyclerView.visibility = View.GONE
                    }

                    // Hide progress bar after data is loaded
                    progressBar.visibility = View.GONE

                    Log.d("ApplicationsFragment", "Application list size after data load: ${applicationList.size}")

                    // Notify the adapter that data has been changed
                    applicationAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ApplicationsFragment", "Error fetching applications", error.toException())
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "Error loading applications. Please try again later."
                    applicationsRecyclerView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            })
    }
}