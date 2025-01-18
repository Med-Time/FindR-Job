package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button

class JobProvider : AppCompatActivity() {

    private lateinit var btnfloat: FloatingActionButton
    private lateinit var profile: Button
    private lateinit var addjob: Button
    private lateinit var managejob: Button
    private lateinit var viewApplicationsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_provider)

        // Initialize buttons and set onClickListeners
        profile = findViewById(R.id.button_profile)
        profile.setOnClickListener {
            val profileIntent = Intent(this, ProviderAccountDetails::class.java)
            startActivity(profileIntent)
        }

        addjob = findViewById(R.id.button_add_job)
        addjob.setOnClickListener {
            val addJobIntent = Intent(this, InsertDataJobProvider::class.java)
            startActivity(addJobIntent)
        }
        managejob = findViewById(R.id.button_manage_jobs)
        managejob.setOnClickListener {
            val manageJobIntent = Intent(this, JobProviderDashboard::class.java)
            startActivity(manageJobIntent)
        }

        viewApplicationsButton = findViewById(R.id.button_view_applications)
        viewApplicationsButton.setOnClickListener {
            val viewApplicationsIntent = Intent(this, viewapplicationdashborad::class.java)
            startActivity(viewApplicationsIntent)
        }

        // Floating Action Button
        btnfloat = findViewById(R.id.fab)
        btnfloat.setOnClickListener {
            val floatIntent = Intent(this@JobProvider, InsertDataJobProvider::class.java)
            startActivity(floatIntent)
        }
    }
}
