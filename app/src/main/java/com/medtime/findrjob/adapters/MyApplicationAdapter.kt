package com.medtime.findrjob.adapters

import android.annotation.SuppressLint
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
import com.medtime.findrjob.model.ApplicationData

class MyApplicationAdapter(
    private val applications: MutableList<ApplicationData>
) : RecyclerView.Adapter<MyApplicationAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        holder.bind(application)

        holder.fileButton.setOnClickListener {
            if (!application.fileUrl.isNullOrEmpty()) {
                // Check if it's a valid URL and handle PDF viewing
                if (application.fileUrl.endsWith(".pdf", ignoreCase = true)) {
                    // Open PDF in an external viewer
                    openPdfFile(holder, application.fileUrl)
                } else {
                    // Handle other file types if necessary
                    openUrl(holder, application.fileUrl)
                }
            } else {
                Toast.makeText(holder.itemView.context, "No file attached to this application.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
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
        private val applicantEmail: TextView = itemView.findViewById(R.id.applicantEmail)
        private val applicantContact: TextView = itemView.findViewById(R.id.applicantContact)
        private val applicantAddress: TextView = itemView.findViewById(R.id.applicantAddress)
        private val jobCompany: TextView = itemView.findViewById(R.id.jobCompany)
        private val jobDate: TextView = itemView.findViewById(R.id.jobDate)
        val fileButton: Button = itemView.findViewById(R.id.fileButton)

        @SuppressLint("SetTextI18n")
        fun bind(application: ApplicationData) {
            jobTitle.text = application.jobTitle ?: "No Title"
            applicationStatus.text = "Status : ${application.status?: "Pending"}"
            applicantEmail.text = "Email : ${application.email ?: "N/A"}"
            applicantContact.text = "Contact : ${application.contact ?: "N/A"}"
            applicantAddress.text = "Address : ${application.address ?: "N/A"}"
            jobCompany.text = "Company : ${application.company ?: "Unknown"}"
            jobDate.text = "Applied Date : ${application.date ?: "N/A"}"

            // Show file button only if CV URL is present
            fileButton.visibility = if (!application.fileUrl.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    fun updateList(newList: List<ApplicationData>) {
        applications.clear()
        applications.addAll(newList)
        notifyDataSetChanged()
    }
}
