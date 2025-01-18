package com.medtime.findrjob.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.medtime.findrjob.R
import com.medtime.findrjob.model.Application
import com.medtime.findrjob.model.ApplicationData
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ViewApplicationProviderAdapter(
    private val applicationList: ArrayList<Application>,
    private val context: Context
) : RecyclerView.Adapter<ViewApplicationProviderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemjobapplication, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applicationList[position]
        holder.fullName.text = application.name
        holder.address.text = application.address
        holder.contactDetail.text = application.contact
        holder.emailAddress.text = application.email
        holder.cvUrl.text = application.cvUrl

        holder.btnDownloadCv.setOnClickListener {
            downloadAndOpenCv(application.cvUrl)
        }

        holder.cvUrl.setOnClickListener {
            val url = application.cvUrl
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return applicationList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullName: TextView = itemView.findViewById(R.id.fullName)
        val address: TextView = itemView.findViewById(R.id.address)
        val contactDetail: TextView = itemView.findViewById(R.id.contactDetail)
        val emailAddress: TextView = itemView.findViewById(R.id.emailAddress)
        val cvUrl: TextView = itemView.findViewById(R.id.cvUrl)
        val btnDownloadCv: Button = itemView.findViewById(R.id.btnDownloadCv)
    }

    private fun downloadAndOpenCv(cvUrl: String?) {
        DownloadCvTask().execute(cvUrl)
    }

    private inner class DownloadCvTask : AsyncTask<String, Void, File?>() {
        override fun doInBackground(vararg urls: String?): File? {
            val fileUrl = urls[0]
            var cvFile: File? = null
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                cvFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CV.pdf")
                val outputStream = FileOutputStream(cvFile)

                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }

                outputStream.close()
                inputStream.close()
            } catch (e: Exception) {
                Log.e("DownloadCvTask", "Error downloading CV", e)
            }
            return cvFile
        }

        override fun onPostExecute(cvFile: File?) {
            if (cvFile != null) {
                openCvFile(cvFile)
            } else {
                Toast.makeText(context, "Failed to download CV", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCvFile(cvFile: File) {
        val cvUri: Uri = FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.provider", cvFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(cvUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}