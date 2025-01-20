package com.medtime.findrjob.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.medtime.findrjob.EditJobPostActivity
import com.medtime.findrjob.model.JobPost
import com.medtime.findrjob.R


class JobPostAdapter(
    private val jobList: MutableList<JobPost>,  // Make the list mutable if you need to remove items
    private val context: Context,
    private val jobPostDatabase: DatabaseReference
) : RecyclerView.Adapter<JobPostAdapter.JobPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_jobs_item, parent, false)
        return JobPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
        val jobPost = jobList[position]
        holder.title.text = jobPost.title
        holder.salary.text = jobPost.salary
        holder.skills.text = jobPost.skills
        holder.date.text = jobPost.date
        holder.description.text = jobPost.description
        holder.itemView.tag = jobPost.jobId

        // Handle the Edit Button click
        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, EditJobPostActivity::class.java).apply {
                putExtra("jobTitle", jobPost.title)
                putExtra("jobDescription", jobPost.description)
                putExtra("jobSalary", jobPost.salary)
                putExtra("jobSkills", jobPost.skills)
                putExtra("jobDate", jobPost.date)
                putExtra("jobId", jobPost.jobId) // Pass jobId if necessary
            }
            context.startActivity(intent)
        }

        // Handle the Delete Button click
        holder.btnDelete.setOnClickListener {
            val jobIdToDelete = jobPost.jobId
            val providerId = FirebaseAuth.getInstance().currentUser?.uid

            if (providerId != null) {
                // Correct the reference to match the database structure
                val jobPostRef = jobPostDatabase.child(providerId).child(jobIdToDelete)

                jobPostRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Remove item from list and notify adapter
                        jobList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, jobList.size)

                        Toast.makeText(
                            holder.itemView.context,
                            "Job post deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("del", "Error deleting job post: ${task.exception?.message}")
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to delete job post",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Log.e("del", "No provider ID or job ID found")
                Toast.makeText(
                    holder.itemView.context,
                    "Failed to delete job post. Missing information.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    fun updateList(newList: List<JobPost>) {
        jobList.clear()
        jobList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class JobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.jobTitle)
        val salary: TextView = itemView.findViewById(R.id.jobSalary)
        val skills: TextView = itemView.findViewById(R.id.jobSkills)
        val date: TextView = itemView.findViewById(R.id.jobDate)
        val description: TextView = itemView.findViewById(R.id.jobDescription)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }
}

