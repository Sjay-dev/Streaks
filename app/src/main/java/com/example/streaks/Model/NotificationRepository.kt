package com.example.streaks.Model

import com.example.streaks.Model.DataBase.NotificationDao
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    fun getAllNotifications(): Flow<List<NotificationModel>> = dao.getAllNotifications()

    suspend fun addNotification(notification: NotificationModel): Long {
        return dao.insert(notification)
    }

    suspend fun updateNotificationStatus(id: Int, status: Status) {
        dao.updateStatus(id, status)
    }

    suspend fun clearNotifications() {
        dao.clearAll()
    }

    suspend fun deleteNotification(id: Int) {
        dao.deleteNotification(id)
    }
}