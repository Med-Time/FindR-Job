package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.medtime.findrjob.fragments.ApplicationsFragment
import com.medtime.findrjob.fragments.JobsFragment

class JobSeekerDashboard : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_seeker_dashboard)
        setupEdgeInsets()
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            loadFragment(JobsFragment())
        }
        // Handle navigation
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_jobs -> loadFragment(JobsFragment())
                R.id.nav_applications -> loadFragment(ApplicationsFragment())
                R.id.nav_profile -> {
                    startActivity(Intent(this, SeekerAccountDetails::class.java))
                }
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            super.onBackPressed() // Close the app
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }
        builder.setCancelable(true)
        builder.create().show()
    }
}
