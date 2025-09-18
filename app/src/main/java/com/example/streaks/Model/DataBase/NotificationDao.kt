package com.example.streaks.Model.DataBase

import androidx.room.Dao
import androidx.room.*
import androidx.room.OnConflictStrategy
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import com.example.streaks.Model.StreakModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationModel): Long

    @Query("SELECT * FROM Notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Query("UPDATE Notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Status)

    @Query("DELETE FROM Notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)

    @Query("DELETE FROM Notifications")
    suspend fun clearAll()

    @Query("SELECT * FROM Notifications WHERE streakId = :id")
    suspend fun getStreakById(id: Int)

}
