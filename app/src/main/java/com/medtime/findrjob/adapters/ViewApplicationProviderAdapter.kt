package com.medtime.findrjob.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medtime.findrjob.R
import com.medtime.findrjob.model.ApplicationData

class ViewApplicationProviderAdapter(
    private val applicationList: MutableList<ApplicationData>,
    private val onApplicationClick: (ApplicationData) -> Unit
) : RecyclerView.Adapter<ViewApplicationProviderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_job_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applicationList[position]
        holder.jobTitle.text = application.jobTitle
        holder.fullName.text = application.name
        holder.address.text = application.address
        holder.emailAddress.text = application.email
        holder.date.text = "Applied on : ${application.date}"
        holder.itemView.setOnClickListener { onApplicationClick(application)}
    }

    override fun getItemCount(): Int {
        return applicationList.size
    }

    fun updateList(filteredList: List<ApplicationData>) {
        applicationList.clear()
        applicationList.addAll(filteredList)
        notifyDataSetChanged()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.job_title)
        val fullName: TextView = itemView.findViewById(R.id.fullName)
        val address: TextView = itemView.findViewById(R.id.address)
        val emailAddress: TextView = itemView.findViewById(R.id.emailAddress)
        val date: TextView = itemView.findViewById(R.id.jobDate)
    }
}