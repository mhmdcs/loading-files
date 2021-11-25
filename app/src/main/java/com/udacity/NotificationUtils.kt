package com.udacity

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

private const val REQUEST_CODE = 1
private const val NOTIFICATION_ID = 1

fun NotificationManager.sendNotification(
    downloadState: DownloadState,
    fileName: String,
    context: Context
) {
    val intent = Intent(context, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("fileName", fileName)
        putExtra("downloadState", downloadState.asString())
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val checkStatusAction = NotificationCompat.Action.Builder(
        null,
        context.getString(R.string.notification_action_check_status),
        pendingIntent
    ).build()


    val builder = NotificationCompat.Builder(
        context,
        context.getString(R.string.notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .addAction(checkStatusAction)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    notify(NOTIFICATION_ID, builder.build())
}

@RequiresApi(Build.VERSION_CODES.O)
fun NotificationManager.createChannel(context: Context) {
    Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.run {
        val notificationChannel = NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.description = context.getString(R.string.notification_channel_description)
        notificationChannel.setShowBadge(true)
        createNotificationChannel(notificationChannel)
    }
}