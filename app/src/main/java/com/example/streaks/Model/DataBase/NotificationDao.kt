package com.example.streaks.Model.DataBase

import androidx.room.Dao
import androidx.room.*
import androidx.room.OnConflictStrategy
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationModel): Long

    @Query("SELECT * FROM Notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Query("DELETE FROM Notifications")
    suspend fun clearAll()

    @Query("UPDATE Notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Status)
}
