package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val streakId = intent.getIntExtra("streakId", -1)

        if (streakId == -1) return

        when (action) {
            ACTION_MARK_DONE -> {
                // TODO: update streak progress in DB
                Toast.makeText(context, "Streak marked as done!", Toast.LENGTH_SHORT).show()
                cancelNotification(context, streakId)
            }

            ACTION_SNOOZE -> {
                // TODO: reschedule notification for +10 minutes or user-defined snooze
                Toast.makeText(context, "Reminder snoozed!", Toast.LENGTH_SHORT).show()
                cancelNotification(context, streakId)
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun cancelNotification(context: Context, streakId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(streakId) // use streakId as notificationId
    }

    companion object {
        const val ACTION_MARK_DONE = "com.yourapp.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.yourapp.ACTION_SNOOZE"
    }
}
