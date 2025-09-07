package com.example.streaks.Model.DataBase

import com.example.streaks.Model.NotificationModel
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    fun getAllHistory(): Flow<List<NotificationModel>> = dao.getAllHistory()

    suspend fun insert(notification: NotificationModel) {
        dao.insert(notification)
    }

    suspend fun updateStatus(id: Int, status: String) {
        dao.updateStatus(id, status)
    }

    suspend fun delete(notification: NotificationModel) {
        dao.delete(notification)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
