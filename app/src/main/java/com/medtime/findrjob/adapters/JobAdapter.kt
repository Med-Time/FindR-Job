package com.medtime.findrjob.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.medtime.findrjob.JobApplicationActivity
import com.medtime.findrjob.R
import com.medtime.findrjob.model.Job

class JobAdapter(private val jobList: List<Job>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitle: TextView = itemView.findViewById(R.id.job_title)
        val jobCompany: TextView = itemView.findViewById(R.id.job_company)
        val jobDescription: TextView = itemView.findViewById(R.id.jobDescription)
        val jobDate: TextView = itemView.findViewById(R.id.jobDate)
        val jobSkills: TextView = itemView.findViewById(R.id.jobSkills)
        val jobSalary: TextView = itemView.findViewById(R.id.jobSalary)
        val btnApply: Button = itemView.findViewById(R.id.btn_apply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        // Inflate the layout for each job post
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alljobpost, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]

        // Set the job details into the views
        val jobId = job.id
        holder.jobTitle.text = job.title
//        holder.jobCompany.text = job.company
        holder.jobDescription.text = job.description
        holder.jobDate.text = job.date
        holder.jobSkills.text = job.skills
        holder.jobSalary.text = job.salary

        // Set up the Apply button's click listener
        holder.btnApply.setOnClickListener {
            // Handle Apply button click (you can add your logic here, e.g., opening a job details page, etc.)
            Toast.makeText(holder.itemView.context, "Apply button clicked for ${job.title}", Toast.LENGTH_SHORT).show()

            val intent = Intent(holder.itemView.context, JobApplicationActivity::class.java)
            intent.putExtra("jobTitle", job.title)
            intent.putExtra("jobId", jobId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = jobList.size
}
