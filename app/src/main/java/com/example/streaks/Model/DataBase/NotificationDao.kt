package com.example.streaks.Model.DataBase

import androidx.room.*
import com.example.streaks.Model.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationModel)

    @Query("SELECT * FROM streak_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

@Delete
suspend fun deleteNotification(notification: NotificationModel)

    @Query("DELETE FROM streak_notifications")
    suspend fun clearAll()
}