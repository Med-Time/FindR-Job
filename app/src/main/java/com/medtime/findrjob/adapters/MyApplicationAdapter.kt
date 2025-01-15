package com.medtime.findrjob.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.medtime.findrjob.R
import com.medtime.findrjob.model.Application

class MyApplicationAdapter(
    private val applications: List<Application> // Use immutable List
) : RecyclerView.Adapter<MyApplicationAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        Log.d("MyApplicationAdapter", "onCreateViewHolder called.")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        Log.d("MyApplicationAdapter", "onBindViewHolder called for position $position")
        val application = applications[position]
        holder.bind(application)

        holder.fileButton.setOnClickListener {
            Log.d("MyApplicationAdapter", "File button clicked for application at position $position")
            if (!application.cvUrl.isNullOrEmpty()) {
                // Check if it's a valid URL and handle PDF viewing
                if (application.cvUrl.endsWith(".pdf", ignoreCase = true)) {
                    // Open PDF in an external viewer
                    openPdfFile(holder, application.cvUrl)
                } else {
                    // Handle other file types if necessary
                    Log.d("MyApplicationAdapter", "Non-PDF file, opening with default viewer")
                    openUrl(holder, application.cvUrl)
                }
            } else {
                Log.d("MyApplicationAdapter", "No CV URL found for application at position $position")
                Toast.makeText(holder.itemView.context, "No file attached to this application.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("MyApplicationAdapter", "getItemCount called, returning size: ${applications.size}")
        return applications.size
    }

    // Function to open PDF file in an external viewer
    private fun openPdfFile(holder: ApplicationViewHolder, pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf")
        try {
            holder.itemView.context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MyApplicationAdapter", "Error opening PDF file: $e")
            Toast.makeText(holder.itemView.context, "Unable to open PDF file.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to open non-PDF URL (can be used for other file types or websites)
    private fun openUrl(holder: ApplicationViewHolder, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            holder.itemView.context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MyApplicationAdapter", "Error opening URL: $e")
            Toast.makeText(holder.itemView.context, "Unable to open the file.", Toast.LENGTH_SHORT).show()
        }
    }

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobTitle: TextView = itemView.findViewById(R.id.jobTitle)
        private val applicationStatus: TextView = itemView.findViewById(R.id.applicationStatus)
        private val applicantName: TextView = itemView.findViewById(R.id.applicantName)
        private val applicantEmail: TextView = itemView.findViewById(R.id.applicantEmail)
        private val applicantContact: TextView = itemView.findViewById(R.id.applicantContact)
        private val applicantAddress: TextView = itemView.findViewById(R.id.applicantAddress)
        val fileButton: Button = itemView.findViewById(R.id.fileButton)

        fun bind(application: Application) {
            Log.d("ApplicationViewHolder", "bind called for application: $application")
            jobTitle.text = application.jobTitle ?: "No Title"
            applicationStatus.text = application.status ?: "Pending"
            applicantName.text = application.name ?: "N/A"
            applicantEmail.text = application.email ?: "N/A"
            applicantContact.text = application.contact ?: "N/A"
            applicantAddress.text = application.address ?: "N/A"
            fileButton.visibility = if (!application.cvUrl.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
}
