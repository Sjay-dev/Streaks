package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.streaks.Model.DataBase.StreakDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val action = intent.action
                val streakId = intent.getIntExtra("streakId", -1)
                val db = StreakDataBase.getDatabase(context)

                if (streakId != -1) {
                    when (action) {
                        ACTION_MARK_DONE -> {
                            db.notificationDao().updateStatus(streakId, "Done")
                            cancelNotification(context, streakId)
                        }
                        ACTION_END_STREAK -> {
                            db.notificationDao().updateStatus(streakId, "Ended")
                            cancelNotification(context, streakId)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun cancelNotification(context: Context, streakId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(streakId)
    }

    companion object {
        const val ACTION_MARK_DONE = "com.yourapp.ACTION_MARK_DONE"
        const val ACTION_END_STREAK = "com.yourapp.ACTION_END_STREAK"
    }
}
