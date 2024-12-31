package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.medtime.findrjob.Model.Provider

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
        editTextCompanyName = findViewById(R.id.editTextCompanyName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextAddress = findViewById(R.id.editTextAddress)
        editTextIndustryType = findViewById(R.id.editTextIndustryType)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonSave = findViewById(R.id.buttonSave)
        uploadLogoButton = findViewById(R.id.uploadLogoButton)

        // Initialize Firebase database and storage references
        userId = intent.getStringExtra("userId")
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Provider::class.java)
                user?.let {
                    populateProviderDetails(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProviderAccountDetails, "Failed to load provider details.", Toast.LENGTH_SHORT).show()
            }
        })
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
                if (logoUri != null) uploadLogo(logoUri!!)
            } else {
                Toast.makeText(this, "Failed to save details!", Toast.LENGTH_SHORT).show()
            }
        }

        enableEditing(false)
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
        // Create a URI from the logo URL and load the image directly into the ImageView
        val logoUri = Uri.parse(logoUrl)
        imageViewLogo.setImageURI(logoUri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            logoUri = data.data
            Toast.makeText(this, "Logo selected for upload.", Toast.LENGTH_SHORT).show()
        }
    }
}
