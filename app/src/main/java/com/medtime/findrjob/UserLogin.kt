package com.medtime.findrjob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
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
    private var userType: String? = null

    // Google Sign-In setup
    private lateinit var googleSignInClient: GoogleSignInClient

    public override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            navigateToDashboard(userId!!)
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
                return@setOnClickListener
            }

            if (passwordInput.isEmpty()) {
                Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null && currentUser.isEmailVerified) {
                            userId = currentUser.uid
                            val userType = handleRadioButtonClick(findViewById(userTypeGroup.checkedRadioButtonId))
                            when (userType) {
                                "Job Seeker" -> {
                                    val intent = Intent(applicationContext, JobSeekerDashboard::class.java)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                    finish()
                                }
                                "Job Provider" -> {
                                    val intent = Intent(applicationContext, JobProviderDashboard::class.java)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(
                                        this,
                                        "User type is invalid. Please contact support.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Please Verify Your Email Address",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
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

    private fun navigateToDashboard(userId: String) {
        val userDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userDatabase.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val userType = dataSnapshot.child("userType").getValue(String::class.java)
                when (userType) {
                    "Job Seeker" -> {
                        val intent = Intent(applicationContext, JobSeekerDashboard::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                    "Job Provider" -> {
                        val intent = Intent(applicationContext, JobProviderDashboard::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            "User type is invalid. Please contact support.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show()
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
                val account = task.result
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } else {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        userId = currentUser.uid
                        checkIfUserExists(userId!!)
                    }
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun checkIfUserExists(userId: String) {
        val userDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        userDatabase.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                navigateToDashboard(userId)
            } else {
                // User does not exist, redirect to SelectUserType
                redirectToSelectUserType()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking user existence", Toast.LENGTH_SHORT).show()
        }
    }
    private fun redirectToSelectUserType() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val intent = Intent(this, SelectUserType::class.java).apply {
            putExtra("userId", userId)
            putExtra("email", account?.email)
        }
        startActivity(intent)
        finish()
    }

    private fun handleRadioButtonClick(view: View): String? {
        if (view is RadioButton) {
            val checked = view.isChecked
            if (checked) {
                userType = view.text.toString()
            }
        }
        return userType
    }

}
