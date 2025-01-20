package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medtime.findrjob.fragments.ApplicationsFragmentProvider
import com.medtime.findrjob.fragments.ManageJobProvider

class JobProviderDashboard : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_provider_dashboard)
        setupEdgeInsets()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_provider)
        val floatingBtn = findViewById<FloatingActionButton>(R.id.fabprovider)

        if (savedInstanceState == null) {
            loadFragment(ManageJobProvider())
        }

        val userId = intent.getStringExtra("userId")

        // Handle navigation
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_jobs_provider -> loadFragment(ManageJobProvider())
                R.id.nav_applications_provider -> loadFragment(ApplicationsFragmentProvider())
                R.id.nav_profile_provider -> {
                    val profileIntent = Intent(this, ProviderAccountDetails::class.java)
                    profileIntent.putExtra("userId", userId)
                    startActivity(profileIntent)
                }
            }
            true
        }

        floatingBtn.setOnClickListener {
            val postJobIntent = Intent(this, PostJobActivity::class.java)
            postJobIntent.putExtra("userId", userId)
            startActivity(postJobIntent)
        }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val builder = AlertDialog.Builder(this@JobProviderDashboard)
                builder.setTitle("Exit App")
                builder.setMessage("Are you sure you want to exit?")
                builder.setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    finish()  // Close the app
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Close the dialog
                }
                builder.setCancelable(true)
                builder.create().show()
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_provider, fragment)
            .commit()
    }
}
