package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class JobPlatformActivity : AppCompatActivity() {
    private lateinit var jobProvider: ImageView
    private lateinit var jobSeeker: ImageView
    private lateinit var aboutMe: ImageView
    private lateinit var accountDetails: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var logout: ImageButton
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_platform)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        aboutMe = findViewById(R.id.realProfileImage)
        accountDetails = findViewById(R.id.accountDetailsImage)
        logout = findViewById(R.id.logoutButton)
        jobProvider = findViewById(R.id.jobProviderImage)
        jobSeeker = findViewById(R.id.jobSeekerImage)
        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FindR Job Platform"

        userId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser?.uid

        accountDetails.setOnClickListener {
            Log.d("JobPlatformActivity", "User ID: $userId")
            val userDatabase = userId?.let { FirebaseDatabase.getInstance().getReference("Users").child(it) }
            userDatabase?.get()?.addOnSuccessListener {
                if (it.child("userType").value == "Job Seeker") {
                    val accountDetailsIntent = Intent(this, SeekerAccountDetails::class.java)
                    accountDetailsIntent.putExtra("userId", userId)
                    startActivity(accountDetailsIntent)
                } else {
                    val accountDetailsIntent = Intent(this, ProviderAccountDetails::class.java)
                    accountDetailsIntent.putExtra("userId", userId)
                    startActivity(accountDetailsIntent)
                }
            }
        }

        aboutMe.setOnClickListener {
            val aboutUsIntent = Intent(this, AboutUs::class.java)
            startActivity(aboutUsIntent)
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val logoutIntent = Intent(this, UserLogin::class.java)
            startActivity(logoutIntent)
            finish()
        }

        jobSeeker.setOnClickListener {
            val jobSeekerIntent = Intent(this, JobSeekerDashboard::class.java)
            startActivity(jobSeekerIntent)
        }

        jobProvider.setOnClickListener {
            val jobProviderIntent = Intent(this, newjobproviderdashboard::class.java)
            Log.d("Going", "To the next Activity")
            startActivity(jobProviderIntent)
        }
    }
}
