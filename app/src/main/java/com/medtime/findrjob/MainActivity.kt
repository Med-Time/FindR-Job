package com.medtime.findrjob

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var loginImage: ImageView
    private lateinit var registerImage: ImageView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        loginImage = findViewById(R.id.userLoginID)
        registerImage = findViewById(R.id.userRegisterID)

        loginImage.setOnClickListener {
            val intent = Intent(this, UserLogin::class.java)
            startActivity(intent)
        }

        registerImage.setOnClickListener {
            val intent = Intent(this, UserRegister::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!::firebaseAuth.isInitialized) {
            firebaseAuth = FirebaseAuth.getInstance()
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, JobPlatformActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
