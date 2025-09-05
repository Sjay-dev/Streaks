package com.example.streaks.Model.DataBase

import androidx.room.Dao
import androidx.room.*
import androidx.room.OnConflictStrategy
import com.example.streaks.Model.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationModel)

    @Query("SELECT * FROM Notifications ORDER BY time DESC")
    fun getAllHistory(): Flow<List<NotificationModel>>

    @Query("UPDATE Notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Delete
    suspend fun delete(notification: NotificationModel)

    @Query("DELETE FROM Notifications WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM Notifications")
    suspend fun clearAll()

}