package com.medtime.findrjob

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupEdgeInsets()
        displayRoundedLogo()

        // Navigate to login after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, UserLogin::class.java))
            finish()
        }, 2000) // Increased delay for better UX
    }

    private fun setupEdgeInsets() {
        val mainView = findViewById<ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun displayRoundedLogo() {
        val logoImageView = findViewById<ImageView>(R.id.logoImage)
        val drawable = resources.getDrawable(R.drawable.logo, null)
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val roundedBitmap = bitmap.config?.let {
                Bitmap.createBitmap(
                    bitmap.width, bitmap.height, it
                )
            }
            val paint = Paint().apply {
                isAntiAlias = true
                shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            roundedBitmap?.let { Canvas(it) }?.drawRoundRect(
                RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()),
                100F, 100F, paint
            )
            logoImageView.setImageBitmap(roundedBitmap)
        }
    }
}
