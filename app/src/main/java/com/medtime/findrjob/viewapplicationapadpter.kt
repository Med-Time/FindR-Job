package com.medtime.findrjob

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ViewApplicationAdapter(
    private val jobApplications: List<JobApplication>
) : RecyclerView.Adapter<ViewApplicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullName: TextView = view.findViewById(R.id.fullName)
        val address: TextView = view.findViewById(R.id.address)
        val contactDetail: TextView = view.findViewById(R.id.contactDetail)
        val emailAddress: TextView = view.findViewById(R.id.emailAddress)
        val cvUrl: TextView = view.findViewById(R.id.cvUrl)
        val btnDownloadCv: Button = view.findViewById(R.id.btnDownloadCv)

        init {
            // Adjust padding for system bars using WindowInsets
            ViewCompat.setOnApplyWindowInsetsListener(itemView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemjobapplication, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobApplication = jobApplications[position]
        holder.fullName.text = jobApplication.name
        holder.address.text = jobApplication.address
        holder.contactDetail.text = jobApplication.contact
        holder.emailAddress.text = jobApplication.email
        holder.cvUrl.text = jobApplication.cvUrl

        // Make CV URL clickable
        holder.cvUrl.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(jobApplication.cvUrl))
            it.context.startActivity(browserIntent)
        }

        // Handle Download CV button click
        holder.btnDownloadCv.setOnClickListener {
            // Add your logic to download the CV
        }
    }

    override fun getItemCount(): Int = jobApplications.size
}


