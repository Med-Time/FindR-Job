package com.medtime.findrjob

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.medtime.findrjob.JobSeeker.AllJobPostViewHolder
import com.medtime.findrjob.model.JobPost
import com.medtime.findrjob.model.JobPostData
import com.medtime.findrjob.adapters.JobPostAdapter

class newjobprovider : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var jobPost: DatabaseReference
    private lateinit var providerRef: DatabaseReference
//    private lateinit var adapter: FirebaseRecyclerAdapter<JobPostData, AllJobPostViewHolder>
    private lateinit var searchView: SearchView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobPostAdapter
    private val jobList = ArrayList<JobPost>()
    private lateinit var jobsDatabase: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var valueEventListener: ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_newjobprovider)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}