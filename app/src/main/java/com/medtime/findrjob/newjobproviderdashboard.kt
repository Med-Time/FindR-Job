package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.medtime.findrjob.fragments.ApplicationsFragment
import com.medtime.findrjob.fragments.ApplicationsFragmentProvider
import com.medtime.findrjob.fragments.JobsFragment
import com.medtime.findrjob.fragments.JobsFragmentProvider
import com.medtime.findrjob.fragments.ProfileFragment
import com.medtime.findrjob.fragments.ProfileFragmentProvider


class newjobproviderdashboard : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_newjobproviderdashboard)
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_provider)
            if (savedInstanceState == null) {
                loadFragment(ProviderMain())
            }

            // Handle navigation
            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_jobs_provider-> loadFragment(JobsFragmentProvider())
                    R.id.nav_applications_provider-> loadFragment(ApplicationsFragmentProvider())
                    R.id.nav_profile_provider-> {
                        loadFragment(ProfileFragmentProvider())
                    }
                }
                true
            }
        }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_provider, fragment)
            .commit()
    }
    }


