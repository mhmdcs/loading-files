package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

    private var downloadID: Long = 0
    private var fileName = ""
    private var downloadObserver: ContentObserver? = null

    private lateinit var downloadState: DownloadState

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            unregisterDownloadObserver()
            downloadState.takeIf { it != DownloadState.Unknown }?.run {
                sendLoadNotification()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendLoadNotification() {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.createChannel(applicationContext)
        notificationManager.sendNotification(downloadState, fileName, applicationContext)
    }

    private fun unregisterDownloadObserver() {
        contentResolver.unregisterContentObserver(downloadObserver!!)
        downloadObserver = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        //set onClickListener on custom  view button
        custom_button.setOnClickListener {
            loadingButtonListener()
        }
    }

    private fun loadingButtonListener() = when(radioGroupLoad.checkedRadioButtonId) {
        View.NO_ID ->
            Toast.makeText(
                this,
                "Please choose a file to download",
                Toast.LENGTH_SHORT
            ).show()
        else -> {
            fileName =
                findViewById<RadioButton>(radioGroupLoad.checkedRadioButtonId)
                    .text.toString()
            download()
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setAllowedOverMetered(true)
                .setRequiresCharging(false)
                .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        registerObserver(downloadManager)
    }

    private fun registerObserver(downloadManager: DownloadManager) {
        downloadObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadStatus(downloadManager)
            }
        }

        contentResolver.registerContentObserver(
            "content://downloads/".toUri(),
            true,
            downloadObserver!!
        )
    }

    private fun downloadStatus(downloadManager: DownloadManager) {
        downloadManager.query(DownloadManager.Query().setFilterById(downloadID)).also {
            with(it) {
                if (this != null && moveToFirst()) {
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        //when running
                        DownloadManager.STATUS_RUNNING -> {
                            custom_button.changeButtonState(ButtonState.Loading)
                        }

                        //when failed
                        DownloadManager.STATUS_FAILED -> {
                            custom_button.changeButtonState(ButtonState.Completed)
                            changeDownloadState(DownloadState.Failed)
                        }
                        //when successful
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            custom_button.changeButtonState(ButtonState.Completed)
                            changeDownloadState(DownloadState.Successful)
                        }
                        else -> changeDownloadState(DownloadState.Unknown)
                    }
                }
            }
        }
    }

    private fun changeDownloadState(state: DownloadState) {

        if (!::downloadState.isInitialized || state != downloadState) {
            downloadState = state
        }
    }

}