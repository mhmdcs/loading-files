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
    private lateinit var downloadState: String
    private lateinit var file: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        setDownloadStatus()

        ok_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
       //      start the main activity
                  startActivity(intent)
        }

    }

    private fun setDownloadStatus() {
        file = intent.getStringExtra("fileName").toString()
        downloadState = intent.getStringExtra("downloadState").toString()
        file_name.text = file
        download_state.text = downloadState
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
