package com.medtime.findrjob


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private var searchQueryListener: ((String) -> Unit)? = null

    fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
    }

    fun setSearchQueryListener(listener: ((String) -> Unit)?) {
        searchQueryListener = listener
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        val searchItem = menu.findItem(R.id.search_view)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQueryListener?.invoke(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchQueryListener?.invoke(it) }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                showAboutUs()
                return true
            }
            R.id.report -> {
                reportIssue()
                return true
            }
            R.id.logout -> {
                logoutUser()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    open fun logoutUser() {
        // Confirm the user wants to logout using AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Sign out the user from FirebaseAuth
                FirebaseAuth.getInstance().signOut()

                // Navigate to the login screen
                val logoutIntent = Intent(this, UserLogin::class.java)
                logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
                finish()
            }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }


    open fun showAboutUs() {
        val aboutIntent = Intent(this, AboutUs::class.java)
        startActivity(aboutIntent)
    }

    open fun reportIssue() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Report Issue") // Default subject
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please describe the issue you are facing.") // Default body text
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmyprojectagain@gmail.com")) // Default email address

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    fun setupEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}
