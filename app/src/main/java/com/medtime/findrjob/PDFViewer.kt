package com.medtime.findrjob

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException

class PDFViewer : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var pdfPage: PdfRenderer.Page
    private var currentPageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        val pdfFilePath = intent.getStringExtra("pdfFilePath")

        if (!pdfFilePath.isNullOrEmpty()) {
            val file = File(pdfFilePath)
            if (file.exists()) {
                try {
                    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    pdfRenderer = PdfRenderer(fileDescriptor)

                    // Load the first page
                    loadPage(currentPageIndex)
                } catch (e: IOException) {
                    showErrorAndExit("Error opening PDF file: ${e.message}")
                }
            } else {
                showErrorAndExit("The PDF file does not exist.")
            }
        } else {
            showErrorAndExit("Failed to load PDF. File path is null.")
        }
    }

    private fun loadPage(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= pdfRenderer.pageCount) {
            showErrorAndExit("Invalid page index.")
            return
        }

        // Close the previous page if it's open
        pdfPage?.close()

        // Open the new page
        pdfPage = pdfRenderer.openPage(pageIndex)

        // Create a bitmap to render the page
        val bitmap = Bitmap.createBitmap(pdfPage.width, pdfPage.height, Bitmap.Config.ARGB_8888)

        // Render the page to the bitmap
        pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // Set the rendered bitmap to an ImageView
        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageBitmap(bitmap)
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish() // Close the activity
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close the page and the renderer when done
        pdfPage.close()
        pdfRenderer.close()
    }
}
