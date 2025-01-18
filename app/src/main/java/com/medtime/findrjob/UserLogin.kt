package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.FirebaseDatabase

class UserLogin : AppCompatActivity() {

    private lateinit var forgotPassword: TextView
    private lateinit var register: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnLogin: Button
    private lateinit var googleLoginButton: Button
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var loginProgress: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private var userId: String? = null

    // Google Sign-In setup
    private lateinit var googleSignInClient: GoogleSignInClient

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
        setContentView(R.layout.activity_user_login)

        // Initialize views
        forgotPassword = findViewById(R.id.txt_forgot_password)
        register = findViewById(R.id.txt_register)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        btnLogin = findViewById(R.id.btn_login)
        googleLoginButton = findViewById(R.id.google_login_button)
        loginProgress = findViewById(R.id.userLoginProgressbar)
        firebaseAuth = FirebaseAuth.getInstance()
        // Configure Google Sign-In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Google Sign-In Button
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        // Email/Password login
        btnLogin.setOnClickListener {
            val emailInput = email.text.toString().trim()
            val passwordInput = password.text.toString().trim()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Email Address", Toast.LENGTH_SHORT).show()
            }

            if (passwordInput.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
            }
            val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
            if (selectedUserTypeId == -1) {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedUserType = findViewById<RadioButton>(selectedUserTypeId).text.toString()

            firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null && currentUser.isEmailVerified) {
                            userId = currentUser.uid
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            when (selectedUserType) {
                                "Job Seeker" -> {
                                    val intent =
                                        Intent(applicationContext, JobSeekerDashboard::class.java)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                    finish()
                                }

                                "Job Provider" -> {
                                    val intent = Intent(
                                        applicationContext,
                                        newjobproviderdashboard::class.java
                                    )
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                    finish()
                                }

                            }
                        } else {
                            loginProgress.visibility = View.VISIBLE
                            Toast.makeText(
                                this,
                                "Please Verify Your Email Address",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        loginProgress.visibility = View.VISIBLE
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        forgotPassword.setOnClickListener {
            val forgotIntent = Intent(this, ForgotPassword::class.java)
            startActivity(forgotIntent)
        }

        register.setOnClickListener {
            val registerIntent = Intent(this, UserRegister::class.java)
            startActivity(registerIntent)
        }
    }

    // Start Google Sign-In process
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Handle the result of Google Sign-In
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                // Google Sign-In was successful
                val account = task.result
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } else {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

    // Firebase Authentication with Google Sign-In
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid

                        // Check if the userType exists in the database
                        val userDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                        userDatabase.get().addOnSuccessListener { dataSnapshot ->
                            if (dataSnapshot.exists()) {
                                val userType = dataSnapshot.child("userType").getValue(String::class.java)
                                if (userType == "Job Seeker") {
                                    // If userType is found, proceed to the platform
                                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(applicationContext, JobSeekerDashboard::class.java)
                                    intent.putExtra("userId", userId)
                                    intent.putExtra("userType", userType)  // Pass the user type
                                    startActivity(intent)
                                    finish()
                                }
                                if (userType == "Job Provider") {
                                    // If userType is found, proceed to the platform
                                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(applicationContext, newjobproviderdashboard::class.java)
                                    intent.putExtra("userId", userId)
                                    intent.putExtra("userType", userType)  // Pass the user type
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // If userType is not found, prompt the user to select a user type
                                val intent = Intent(applicationContext, SelectUserType::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun handleRadioButtonSelection(checkedId: Int) {
        val selectedRadioButton = findViewById<android.widget.RadioButton>(checkedId)
        val selectedText = selectedRadioButton.text.toString()
        showToast("Selected: $selectedText")

    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
