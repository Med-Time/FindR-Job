package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button

class JobProvider : AppCompatActivity() {

    private lateinit var btnfloat: FloatingActionButton
    private lateinit var profile: Button
    private lateinit var addjob: Button
    private lateinit var managejob: Button
    private lateinit var viewapplicationsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_provider)
        Log.d("Arrived", "At first")

        // Initialize buttons and set onClickListeners
        profile = findViewById(R.id.button_profile)
        profile.setOnClickListener {
            val ProfileIntent = Intent(this, ProviderAccountDetails::class.java)
            startActivity(ProfileIntent)
        }

        addjob = findViewById(R.id.button_add_job)
        addjob.setOnClickListener {
            val AddjobIntent = Intent(this, InsertDataJobProvider::class.java)
            startActivity(AddjobIntent)
        }

//        managejob = findViewById(R.id.button_manage_jobs)
//        managejob.setOnClickListener {
//            val ManagejobIntent = Intent(this, JobProviderDashboardActivity::class.java)
//            startActivity(ManagejobIntent)
//        }

        viewapplicationsButton = findViewById(R.id.button_view_applications)
        viewapplicationsButton.setOnClickListener {
            val ViewapplicationsIntent = Intent(this, viewapplicationdashborad::class.java)
            startActivity(ViewapplicationsIntent)
        }

        // Floating Action Button
        btnfloat = findViewById(R.id.fab)
        btnfloat.setOnClickListener {
            val floatIntent = Intent(this@JobProvider, InsertDataJobProvider::class.java)
            startActivity(floatIntent)
        }
    }
}
