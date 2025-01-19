package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupEdgeInsets()
        loadLogoWithGlide()

        // Navigate based on login status after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateBasedOnLoginStatus()
        }, 2000)
    }

    private fun setupEdgeInsets() {
        val mainView = findViewById<ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadLogoWithGlide() {
        val logoImageView = findViewById<ImageView>(R.id.logoImage)
        Glide.with(this)
            .load(R.drawable.logo) // Replace with your actual logo resource or URL
            .circleCrop() // Automatically creates rounded images
            .into(logoImageView)
    }

    private fun navigateBasedOnLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is logged in, decide based on user type
            determineUserTypeAndRedirect(currentUser.uid)
        } else {
            // User not logged in, navigate to login screen
            startActivity(Intent(this, UserLogin::class.java))
            finish()
        }
    }

    private fun determineUserTypeAndRedirect(userId: String) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId)

        database.get().addOnSuccessListener { snapshot ->
            val userType = snapshot.child("userType").value as? String
            when (userType) {
                "Job Seeker" -> {
                    val intent = Intent(this, JobSeekerDashboard::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)

                }
                "Job Provider" -> {
                    val intent = Intent(this, JobProviderDashboard::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
                else -> {
                    startActivity(Intent(this, UserLogin::class.java))
                }
            }
            finish()
        }.addOnFailureListener {
            // Handle error case
            startActivity(Intent(this, UserLogin::class.java))
            finish()
        }
    }
}
