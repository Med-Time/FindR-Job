package com.medtime.findrjob

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
import com.medtime.findrjob.model.JobPost

class JobPostAdapter(
    private val jobList: MutableList<JobPost>,  // Make the list mutable if you need to remove items
    private val context: Context,
    private val jobPostDatabase: DatabaseReference
) : RecyclerView.Adapter<JobPostAdapter.JobPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_edit_job_item, parent, false)
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
            Log.d("del", "Provider ID: $providerId, Job ID to delete: $jobIdToDelete")

            if (providerId != null) {
                // Database reference to the specific job post under the provider
                val jobPostRef = jobPostDatabase.child("Job Post").child(providerId).child(jobIdToDelete)

                // Log the database reference to ensure it's correct
                Log.d("del", "Database reference: ${jobPostRef.toString()}")

                // Attempt to remove the job post from Firebase
                jobPostRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Remove from adapter and update the UI
                        jobList.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Job post deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Log the error and show failure message
                        Log.e("del", "Error deleting job post: ${task.exception?.message}")
                        Toast.makeText(context, "Failed to delete job post", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("del", "No provider ID found")
                Toast.makeText(context, "Provider not logged in", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return jobList.size
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

