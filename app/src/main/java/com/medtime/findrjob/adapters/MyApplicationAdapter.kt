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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
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
                viewResume(holder, application.fileUrl)
            } else {
                Toast.makeText(holder.itemView.context, "No file attached to this application.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return applications.size
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
        private val jobMessage: TextView = itemView.findViewById(R.id.jobMessage)

        @SuppressLint("SetTextI18n")
        fun bind(application: ApplicationData) {
            jobTitle.text = application.jobTitle ?: "No Title"
            applicationStatus.text = "Status : ${application.status?: "Pending"}"
            applicantEmail.text = "Email : ${application.email ?: "N/A"}"
            applicantContact.text = "Contact : ${application.contact ?: "N/A"}"
            applicantAddress.text = "Address : ${application.address ?: "N/A"}"
            jobCompany.text = "Company : ${application.company ?: "Unknown"}"
            jobDate.text = "Applied Date : ${application.date ?: "N/A"}"
            jobMessage.text = "Message : ${application.message ?: "N/A"}"

            // Show file button only if CV URL is present
            fileButton.visibility = if (!application.fileUrl.isNullOrEmpty()) View.VISIBLE else View.GONE
            jobMessage.visibility = if (application.status != "Pending") View.VISIBLE else View.GONE

        }
    }

    fun updateList(newList: List<ApplicationData>) {
        applications.clear()
        applications.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeApplication(position: Int): ApplicationData? {
        return if (position >= 0 && position < applications.size) {
            val removedItem = applications.removeAt(position)
            notifyItemRemoved(position)
            removedItem
        } else {
            null
        }
    }

    private fun viewResume(holder: ApplicationViewHolder, resumeUrl: String?) {
        if (resumeUrl != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(resumeUrl), "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                if (intent.resolveActivity(holder.itemView.context.packageManager) != null) {
                    holder.itemView.context.startActivity(intent)
                } else {
                    showToast(holder, "No PDF viewer app found.")
                }
            } catch (e: Exception) {
                showToast(holder, "Error opening PDF: ${e.message}")
            }
        } else {
            showToast(holder, "No resume available.")
        }
    }

    private fun showToast(holder: ApplicationViewHolder, message: String) {
        Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
    }
}
