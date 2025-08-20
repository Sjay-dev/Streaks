//package com.example.streaks.View.NotificationScreens
//
//import android.content.Context
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//
//class StreakReminderWorker(
//    context: Context,
//    workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//        val streakName = inputData.getString("streakName") ?: "Streak"
//
//        val streakId = inputData.getString("streakId") ?: "0"
//
//        showNotification(
//            applicationContext,
//            "Streak Reminder ⏰",
//            "Your streak '$streakName' is about to end. Renew it now!"
//        )
//
//        return Result.success()
//    }
//
//    private fun showNotification(context: Context, title: String, message: String) {
//        val channelId = "streak_channel"
//        val notificationManager = NotificationManagerCompat.from(context)
//
//        val builder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        // Create channel for Android 8+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Streak Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        notificationManager.notify(streakId.hashCode(), builder.build())
//    }
//}
//
//fun scheduleStreakReminder(context: Context, streakId: String, streakName: String, deadline: Long) {
//    val reminderTime = deadline - (60 * 60 * 1000) // 1 hour before deadline
//
//    val delay = reminderTime - System.currentTimeMillis()
//
//    val data = workDataOf(
//        "streakId" to streakId,
//        "streakName" to streakName
//    )
//
//    val request = OneTimeWorkRequestBuilder<StreakReminderWorker>()
//        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
//        .setInputData(data)
//        .build()
//
//    WorkManager.getInstance(context).enqueue(request)
//}
//
