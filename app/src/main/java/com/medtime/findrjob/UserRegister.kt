package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.model.User

class UserRegister : AppCompatActivity() {

    private lateinit var btnregister: Button
    private lateinit var txtlogin: TextView
    private lateinit var fullname: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var cpassword: EditText
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var registerprogress: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_register)

        // Initializing views
        btnregister = findViewById(R.id.btn_register)
        txtlogin = findViewById(R.id.txt_signin)
        fullname = findViewById(R.id.txt_fullname)
        email = findViewById(R.id.txt_email)
        password = findViewById(R.id.txt_password)
        cpassword = findViewById(R.id.txt_cpassword)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        registerprogress = findViewById(R.id.userRegisterProgressbar)

        // Firebase initialization
        firebaseAuth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance().reference.child("Users")

        btnregister.setOnClickListener {
            val fullName = fullname.text.toString().trim()
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()
            val cPassword = cpassword.text.toString().trim()

            // Validation checks
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Fullname", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cPassword != password) {
                Toast.makeText(this, "Password do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected user type
            val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
            if (selectedUserTypeId == -1) {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()
//            Toast.makeText(this, selectedUserType, Toast.LENGTH_SHORT).show()

            // Firebase authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                        firebaseAuth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    val userId = firebaseAuth.currentUser?.uid
                                    val newUser = User(fullName, email, selectedUserType)

                                    // Save user data to Firebase Database
                                    userId?.let {
                                        userDatabase.child(it).setValue(newUser)
                                            .addOnCompleteListener { saveTask ->
                                                if (saveTask.isSuccessful) {
                                                    // Redirect based on user type
                                                    if (selectedUserType == "Job Seeker") {
                                                        val intent = Intent(this, GetSeekerDetails::class.java)
                                                        intent.putExtra("userID", userId)
                                                        startActivity(intent)
                                                        finish()
                                                    } else if (selectedUserType == "Job Provider") {
                                                        val intent = Intent(this, GetProviderDetails::class.java)
                                                        intent.putExtra("userID", userId)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                } else {
                                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } else {
                                    registerprogress.visibility = View.VISIBLE
                                    Toast.makeText(this, "Failed to request verification mail", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        val errorMessage = task.exception?.message
                        Toast.makeText(this, "Registration Failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }

        }

        txtlogin.setOnClickListener {
            val loginIntent = Intent(this, UserLogin::class.java)
            startActivity(loginIntent)
        }
    }
}
