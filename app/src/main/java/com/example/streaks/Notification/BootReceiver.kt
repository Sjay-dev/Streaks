package com.example.streaks.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.streaks.Model.DataBase.StreakDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = StreakDataBase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                db.streakDao().getAllWithReminders().first().forEach { streak ->
                    NotificationScheduler.scheduleNext(context, streak)
                }
            }
        }
    }
}
