package com.example.streaks.Notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.DataBase.StreakDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val streakId = intent.getIntExtra("streakId", -1)
                if (streakId != -1) {
                    val db = StreakDataBase.getDatabase(context)
                    val streak = db.streakDao().getStreakById(streakId)

                    streak?.let {
                        val notification = NotificationHelper.buildNotification(context, it)

                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            NotificationManagerCompat.from(context)
                                .notify(it.streakId, notification)
                        }

                        NotificationScheduler.scheduleNext(context, it)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}


