package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

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

        aboutMe = findViewById(R.id.realProfileImage)
        accountDetails = findViewById(R.id.accountDetailsImage)
        logout = findViewById(R.id.logoutButton)
        jobProvider = findViewById(R.id.jobProviderImage)
        jobSeeker = findViewById(R.id.jobSeekerImage)
        toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FindR Job Platform"

        userId = intent.getStringExtra("userId")

        accountDetails.setOnClickListener {
            val accountDetailsIntent = Intent(this, AccountDetails::class.java)
            accountDetailsIntent.putExtra("userId", userId)
            startActivity(accountDetailsIntent)
        }

        aboutMe.setOnClickListener {
            val aboutMeIntent = Intent(this, AboutMe::class.java)
            startActivity(aboutMeIntent)
            finish()
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val logoutIntent = Intent(this, UserLogin::class.java)
            startActivity(logoutIntent)
            finish()
        }

        jobSeeker.setOnClickListener {
            val jobSeekerIntent = Intent(this, JobSeeker::class.java)
            startActivity(jobSeekerIntent)
        }

        jobProvider.setOnClickListener {
            val jobProviderIntent = Intent(this, JobProvider::class.java)
            startActivity(jobProviderIntent)
        }
    }
}
