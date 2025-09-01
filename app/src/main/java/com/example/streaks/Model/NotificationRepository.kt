package com.example.streaks.Model

import com.example.streaks.Model.DataBase.NotificationDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val dao: NotificationDao
) {
    fun getAllNotifications(): Flow<List<NotificationModel>> = dao.getAllNotifications()

    suspend fun addNotification(notification: NotificationModel){
        dao.insert(notification)
    }

    suspend fun deleteNotification(notification: NotificationModel) {
        dao.deleteNotification(notification)
    }

    suspend fun clearAll()  {
        dao.clearAll()
    }
}