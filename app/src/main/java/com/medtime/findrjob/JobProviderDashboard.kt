package com.medtime.findrjob

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medtime.findrjob.fragments.ApplicationsFragmentProvider
import com.medtime.findrjob.fragments.JobsFragmentProvider
import com.medtime.findrjob.fragments.ManageJobProvider
import com.medtime.findrjob.fragments.ProfileFragmentProvider


class JobProviderDashboard : BaseActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            enableEdgeToEdge()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_newjobproviderdashboard)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_provider)
            val floatingBtn = findViewById<FloatingActionButton>(R.id.fabprovider)
            if (savedInstanceState == null) {
                loadFragment(ManageJobProvider())
            }

            // Handle navigation
            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_jobs_provider-> loadFragment(ManageJobProvider())
                    R.id.nav_applications_provider-> loadFragment(ApplicationsFragmentProvider())
                    R.id.nav_profile_provider-> {
                        loadFragment(ProfileFragmentProvider())
                    }
                }
                true
            }
            floatingBtn.setOnClickListener {
                loadFragment(JobsFragmentProvider())
            }
        }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_provider, fragment)
            .commit()
    }
    }


