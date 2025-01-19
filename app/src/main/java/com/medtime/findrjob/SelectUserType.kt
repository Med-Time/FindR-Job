package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SelectUserType : AppCompatActivity() {
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_user_type)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userTypeGroup = findViewById(R.id.userTypeGroup)
        submitButton = findViewById(R.id.submitButton)
        firebaseAuth = FirebaseAuth.getInstance()
        name = findViewById<EditText>(R.id.name).text.toString()
        val email = intent.getStringExtra("email")
        val userId = intent.getStringExtra("userId")


        submitButton.setOnClickListener {
            val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
            if (selectedUserTypeId == -1) {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
            } else {
                val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()

                val user = mapOf(
                    "email" to email,
                    "fullname" to name,
                    "userType" to selectedUserType
                )

                // Save userType to Firebase
                val userDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId!!)
                userDatabase.setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        when (selectedUserType) {
                            "Job Seeker" -> {
                                val intent = Intent(this, JobSeekerDashboard::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                                finish()
                            }
                            "Job Provider" -> {
                                val intent = Intent(this, JobProviderDashboard::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to save user type", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
