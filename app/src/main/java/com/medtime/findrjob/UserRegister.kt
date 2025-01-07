package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.medtime.findrjob.Model.User

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

    public override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            intent = Intent(applicationContext, JobPlatformActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

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
            val Fullname = fullname.text.toString().trim()
            val Email = email.text.toString().trim()
            val Password = password.text.toString().trim()
            val Cpassword = cpassword.text.toString().trim()

            // Validation checks
            if (Fullname.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Fullname", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (Email.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (Password.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (Cpassword != Password) {
                Toast.makeText(this, "Password do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            registerprogress.visibility = View.VISIBLE
            dialogBox("Fullname: $Fullname Email: $Email  Password: $Password")

            // Get selected user type
            val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
            if (selectedUserTypeId == -1) {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()
            Toast.makeText(this, selectedUserType, Toast.LENGTH_SHORT).show()

            // Firebase authentication
            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "E-mail Created", Toast.LENGTH_SHORT).show()
                        firebaseAuth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    val userId = firebaseAuth.currentUser?.uid
                                    val newUser = User(Fullname, Email, Password)

                                    // Save user data to Firebase Database
                                    userId?.let {
                                        userDatabase.child(it).setValue(newUser)
                                            .addOnCompleteListener { saveTask ->
                                                if (saveTask.isSuccessful) {
                                                    // Redirect based on user type
                                                    if (selectedUserType == "Job Seeker") {
                                                        val intent = Intent(this, GetSeekerDetails::class.java)
                                                        Toast.makeText(this, "Inside Seeker Next is get Seeker Details", Toast.LENGTH_SHORT).show()
                                                        intent.putExtra("userId", userId)
                                                        startActivity(intent)
                                                        finish()
                                                    } else if (selectedUserType == "Job Provider") {
                                                        val intent = Intent(this, GetProviderDetails::class.java)
                                                        Toast.makeText(this, "Inside Provider Next is get Seeker Details", Toast.LENGTH_SHORT).show()
                                                        intent.putExtra("userId", userId)
                                                        startActivity(intent)
                                                        finish()
                                                    }
//                                                    finish()
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

    // Function to show a dialog box(for debugging)
    private fun dialogBox(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage(msg)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
