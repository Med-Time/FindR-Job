package com.medtime.findrjob.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medtime.findrjob.R
import com.medtime.findrjob.model.Job

class JobAdapter(
    private val jobList: MutableList<Job>,
    private val onJobClick: (Job) -> Unit
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.job_title)
        val jobCompany: TextView = itemView.findViewById(R.id.job_company)
        val jobDate: TextView = itemView.findViewById(R.id.jobDate)
        val jobSkills: TextView = itemView.findViewById(R.id.jobSkills)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alljobpost, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
        holder.jobTitle.text = job.title
        holder.jobCompany.text = job.company
        holder.jobDate.text = "Posted on: ${job.date}"
        holder.jobSkills.text = "Skills Required: ${job.skills}"

        holder.itemView.setOnClickListener { onJobClick(job) }
    }

    override fun getItemCount(): Int = jobList.size

    fun updateList(newList: List<Job>) {
        jobList.clear()
        jobList.addAll(newList)
        notifyDataSetChanged()
    }

}
