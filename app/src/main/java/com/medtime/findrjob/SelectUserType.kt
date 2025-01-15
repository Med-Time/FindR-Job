package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

        submitButton.setOnClickListener {
            val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
            if (selectedUserTypeId == -1) {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
            } else {
                val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()

                // Save userType to Firebase
                val userId = firebaseAuth.currentUser?.uid
                val userDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId!!)
                userDatabase.child("userType").setValue(selectedUserType)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User type updated successfully", Toast.LENGTH_SHORT).show()
                            // Redirect to platform or another activity
                            val intent = Intent(this, JobPlatformActivity::class.java)
                            intent.putExtra("userId", userId)
                            intent.putExtra("userType", selectedUserType)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to update user type", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
