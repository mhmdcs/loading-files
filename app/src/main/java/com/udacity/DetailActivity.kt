package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {
    private lateinit var fileName:String
    private lateinit var downloadState:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        setDownloadStatus()

        finish_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // start the main activity
            startActivity(intent)
        }
    }

    private fun setDownloadStatus() {
        fileName = intent.getStringExtra("fileName").toString()
        downloadState = intent.getStringExtra("downloadState").toString()
        file_name_text.text = fileName
        download_state_text.text = downloadState
    }

}
