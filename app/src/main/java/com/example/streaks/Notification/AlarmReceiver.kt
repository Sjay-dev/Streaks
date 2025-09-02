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
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val streakId = intent.getIntExtra("streakId", -1)
        if (streakId == -1) return

        val db = StreakDataBase.getDatabase(context) // ✅ use your StreakDataBase
        CoroutineScope(Dispatchers.IO).launch {
            val streak = db.streakDao().getStreakById(streakId) ?: return@launch

            // Build and show notification
            val notification = NotificationHelper.buildNotification(context, streak)
            NotificationManagerCompat.from(context).notify(streak.streakId, notification)

            // Reschedule the next notification based on frequency
            NotificationScheduler.scheduleNext(context, streak)
        }
    }
}

