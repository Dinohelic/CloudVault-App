package com.cloudvault.cloudvault.ui.activities

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudvault.cloudvault.databinding.ActivityFileViewerBinding

class FileViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.getStringExtra("FILE_NAME")
        val fileUrl = intent.getStringExtra("FILE_URL")
        val fileType = intent.getStringExtra("FILE_TYPE")

        supportActionBar?.title = fileName

        if (fileUrl == null || fileType == null) {
            Toast.makeText(this, "Error: Invalid file details", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        when {
            // More flexible check for any image type
            fileType.contains("image", ignoreCase = true) -> {
                binding.imageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(fileUrl)
                    .into(binding.imageView)
                binding.progressBar.visibility = View.GONE
            }
            // More flexible check for PDF
            fileType.contains("pdf", ignoreCase = true) -> {
                binding.webView.visibility = View.VISIBLE
                binding.webView.webViewClient = WebViewClient()
                binding.webView.settings.javaScriptEnabled = true
                // Use Google's gview to render the PDF in the WebView
                binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$fileUrl")
                binding.progressBar.visibility = View.GONE
            }
            else -> {
                Toast.makeText(this, "This file type cannot be opened in the app.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
