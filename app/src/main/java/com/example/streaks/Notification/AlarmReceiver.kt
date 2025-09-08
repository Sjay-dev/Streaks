package com.example.streaks.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.streaks.Model.DataBase.StreakDataBase
import com.example.streaks.Model.NotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val streakId = intent.getIntExtra("streakId", -1)
                if (streakId != -1) {
                    val db = StreakDataBase.getDatabase(context)

                    val streak = db.streakDao().getStreakById(streakId) ?: return@launch


                    val sysNotification = NotificationHelper.buildNotification(context, streak)

                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        NotificationManagerCompat.from(context)
                            .notify(streak.streakId, sysNotification)
                    }

                    NotificationScheduler.scheduleNext(context, streak)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}


