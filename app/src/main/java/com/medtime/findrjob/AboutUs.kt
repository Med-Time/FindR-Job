package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AboutUs : AppCompatActivity() {
    private lateinit var imgInsta: ImageView
    private lateinit var imgTwitter: ImageView
    private lateinit var imgYoutube: ImageView
    private lateinit var imgGitHub: ImageView
    private lateinit var txtEmail: TextView
    private lateinit var txtWeb: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       enableEdgeToEdge()
        setContentView(R.layout.activity_about_us)

        imgYoutube = findViewById(R.id.imgYoutube)
        imgGitHub = findViewById(R.id.imgGitHub)
        imgInsta = findViewById(R.id.imgInsta)
        imgTwitter = findViewById(R.id.imgTwitter)
        txtEmail = findViewById(R.id.txtEmail)
        txtWeb = findViewById(R.id.txtWeb)

        imgInsta.setOnClickListener {
            openUrl("https://www.instagram.com")
        }

        txtEmail.setOnClickListener {
            sendEmail("mailto:contact@anmolbhusal.cd22@bmsce.ac.in")
        }

        txtWeb.setOnClickListener {
            openUrl("https://bhishanpangeni.com.np")
        }

        imgYoutube.setOnClickListener {
            openUrl("https://youtube.com/@saganepal3541?si=is0tSTfsVT9VSqF_")
        }

        imgGitHub.setOnClickListener {
            openUrl("https://github.com/Med-Time")
        }

        imgTwitter.setOnClickListener {
            openUrl("https://x.com/BhishanPangeni")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    private fun sendEmail(emailUri: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(emailUri)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
