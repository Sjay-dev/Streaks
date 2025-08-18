package com.example.streaks.Model.DataBase

class NotificationRepository {
    private val notifications = mutableListOf<NotificationModel>()

    fun addNotification(notification: NotificationModel) {
        notifications.add(0, notification) // insert latest at top
    }

    fun getAllNotifications(): List<NotificationModel> = notifications
}