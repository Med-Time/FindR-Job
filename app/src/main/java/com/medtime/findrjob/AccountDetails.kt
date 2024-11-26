package com.medtime.findrjob

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.medtime.findrjob.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AccountDetails : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var userDatabase: DatabaseReference
    private lateinit var btnResetPassword: Button
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        userPassword = findViewById(R.id.userPassword)
        btnResetPassword = findViewById(R.id.resetPasswordButton)

        userId = intent.getStringExtra("userId")
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        // Fetch and populate user details
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    userName.setText(it.name)
                    userEmail.setText(it.email)
                    userPassword.setText(it.password)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AccountDetails, "Failed to load user details.", Toast.LENGTH_SHORT).show()
            }
        })

        btnResetPassword.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val email = userEmail.text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@AccountDetails, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AccountDetails, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email field is empty.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
