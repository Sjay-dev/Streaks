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

                    // 🔹 Load the streak from DB
                    val streak = db.streakDao().getStreakById(streakId) ?: return@launch

                    // 🔹 Insert into NotificationModel (history)
                    db.notificationDao().insert(
                        NotificationModel(
                            streakId = streak.streakId,
                            streakName = streak.streakName,
                            message = "Continue Streak?",
                            time = LocalTime.now(),
                            status = "Pending"
                        )
                    )

                    //  Build the system notification
                    val sysNotification = NotificationHelper.buildNotification(context, streak)

                    //  Show notification (if allowed)
                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        NotificationManagerCompat.from(context)
                            .notify(streak.streakId, sysNotification)
                    }

                    // Reschedule for next time
                    NotificationScheduler.scheduleNext(context, streak)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}


