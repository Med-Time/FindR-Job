package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var backLogin: TextView
    private lateinit var email: EditText
    private lateinit var btnForgetPassword: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var forgetPasswordProgressbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        backLogin = findViewById(R.id.txt_back_to_login)
        email = findViewById(R.id.fpemail)
        btnForgetPassword = findViewById(R.id.btn_reset_password)
        firebaseAuth = FirebaseAuth.getInstance()
        forgetPasswordProgressbar = findViewById(R.id.forgetPasswordProgressbar)

        btnForgetPassword.setOnClickListener {
            val emailInput = email.text.toString().trim()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Please Enter the email", Toast.LENGTH_SHORT).show()
            } else {
                forgetPasswordProgressbar.visibility = View.VISIBLE
                firebaseAuth.sendPasswordResetEmail(emailInput).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(applicationContext, UserLogin::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Check Your email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        backLogin.setOnClickListener {
            val backIntent = Intent(this, UserLogin::class.java)
            startActivity(backIntent)
        }
    }
}
