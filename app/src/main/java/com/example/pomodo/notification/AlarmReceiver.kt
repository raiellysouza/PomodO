package com.example.pomodo.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pomodo.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title: String? = intent.getStringExtra("title")
        val message: String? = intent.getStringExtra("message")
        val notificationId: Int = intent.getIntExtra("notificationId", 0)

        if (title == null || message == null) return

        val notification = NotificationCompat.Builder(context, PomodoroNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pomodoro)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
