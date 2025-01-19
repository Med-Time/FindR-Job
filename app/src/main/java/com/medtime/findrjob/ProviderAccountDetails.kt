package com.medtime.findrjob

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.medtime.findrjob.model.Provider

class ProviderAccountDetails : AppCompatActivity() {

    private lateinit var editTextCompanyName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var editTextIndustryType: EditText
    private lateinit var buttonEdit: Button
    private lateinit var buttonSave: Button
    private lateinit var uploadLogoButton: Button
    private lateinit var imageViewLogo: ImageView
    private lateinit var userDatabase: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var userId: String? = null
    private var logoUri: Uri? = null

    companion object {
        private const val STORAGE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_account_details)

        // Initialize views
        imageViewLogo = findViewById(R.id.imageViewLogo)
        editTextCompanyName = findViewById(R.id.textCompanyName)
        editTextEmail = findViewById(R.id.textEmail)
        editTextAddress = findViewById(R.id.textAddress)
        editTextIndustryType = findViewById(R.id.textIndustryType)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonSave = findViewById(R.id.buttonSave)
        uploadLogoButton = findViewById(R.id.uploadLogoButton)

        // Initialize Firebase database and storage references
        userId = intent.getStringExtra("userId")?:FirebaseAuth.getInstance().currentUser?.uid

        userDatabase = FirebaseDatabase.getInstance().getReference("Providers").child(userId!!)
        storageReference = FirebaseStorage.getInstance().getReference("ProviderLogos/$userId/logo.jpg")

        // Fetch and populate provider details
        fetchProviderDetails()

        // Set up button listeners
        buttonEdit.setOnClickListener { enableEditing(true) }
        buttonSave.setOnClickListener { saveProviderData() }
        uploadLogoButton.setOnClickListener { chooseLogo() }

        // Initially disable editing
        enableEditing(false)
    }

    private fun enableEditing(enable: Boolean) {
        editTextCompanyName.isEnabled = enable
        editTextEmail.isEnabled = enable
        editTextAddress.isEnabled = enable
        editTextIndustryType.isEnabled = enable
        uploadLogoButton.visibility = if (enable) View.VISIBLE else View.GONE
        buttonEdit.visibility = if (enable) View.GONE else View.VISIBLE
        buttonSave.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun fetchProviderDetails() {
        val sharedPreferences = getSharedPreferences("${userId}Details", Context.MODE_PRIVATE)
        val companyName = sharedPreferences.getString("companyName", null)
        val email = sharedPreferences.getString("email", null)
        val address = sharedPreferences.getString("address", null)
        val industryType = sharedPreferences.getString("industryType", null)
        val logoUrl = sharedPreferences.getString("logoUrl", null)

        if (companyName != null && email != null && address != null && industryType != null && logoUrl != null) {
            // Load details from SharedPreferences
            val provider = Provider(
                companyName = companyName,
                email = email,
                address = address,
                industryType = industryType,
                logoUrl = logoUrl
            )
            populateProviderDetails(provider)
        } else {
            // Fetch details from Firebase if not available locally
            userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Provider::class.java)
                    user?.let {
                        populateProviderDetails(it)
                        saveDetailsLocally(
                            it.companyName ?: "",
                            it.email ?: "",
                            it.address ?: "",
                            it.industryType ?: "",
                            it.logoUrl ?: ""
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProviderAccountDetails, "Failed to load provider details.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun populateProviderDetails(provider: Provider) {
        editTextCompanyName.setText(provider.companyName)
        editTextEmail.setText(provider.email)
        editTextAddress.setText(provider.address)
        editTextIndustryType.setText(provider.industryType)

        // Load logo from Firebase Storage using URI directly
        if (!provider.logoUrl.isNullOrEmpty()) {
            // If logoUrl exists, load it using Firebase Storage URI
            loadLogoFromFirebase(provider.logoUrl)
        } else {
            imageViewLogo.setImageResource(R.drawable.man_dummy) // Default placeholder image
        }
    }

    private fun saveProviderData() {
        val companyName = editTextCompanyName.text.toString()
        val email = editTextEmail.text.toString()
        val address = editTextAddress.text.toString()
        val industryType = editTextIndustryType.text.toString()

        if (companyName.isEmpty() || email.isEmpty() || address.isEmpty() || industryType.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = Provider(
            companyName = companyName,
            email = email,
            address = address,
            industryType = industryType
        )

        userDatabase.setValue(updatedUser).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Details Saved Successfully!", Toast.LENGTH_SHORT).show()
                saveDetailsLocally(companyName, email, address, industryType, logoUri.toString())
                if (logoUri != null) uploadLogo(logoUri!!)
            } else {
                Toast.makeText(this, "Failed to save details!", Toast.LENGTH_SHORT).show()
            }
        }

        enableEditing(false)
    }

    private fun saveDetailsLocally(
        companyName: String,
        email: String,
        address: String,
        industryType: String,
        logoUrl: String
    ) {
        val sharedPreferences = getSharedPreferences("${userId}Details", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("companyName", companyName)
        editor.putString("email", email)
        editor.putString("address", address)
        editor.putString("industryType", industryType)
        editor.putString("logoUrl", logoUrl)
        editor.apply()
    }



    private fun chooseLogo() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select a Logo"), STORAGE_REQUEST_CODE)
    }

    private fun uploadLogo(uri: Uri) {
        val logoRef = storageReference.child("logo.jpg")  // Use the unique user ID for each logo file
        logoRef.putFile(uri).addOnSuccessListener {
            logoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Update logo URL in the database
                userDatabase.child("logoUrl").setValue(downloadUri.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Logo Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update logo URL in database!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload logo!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLogoFromFirebase(logoUrl: String) {
            Glide.with(this)
                .load(logoUrl)
                .into(imageViewLogo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            logoUri = data.data
            Toast.makeText(this, "Logo selected for upload.", Toast.LENGTH_SHORT).show()
        }
    }
}
