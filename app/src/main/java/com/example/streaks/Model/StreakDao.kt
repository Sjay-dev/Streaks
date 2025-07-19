package com.example.streaks.Model

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStreak(streak: streakModel)

    @Update
    suspend fun updateStreak(streak: streakModel)

    @Delete
    suspend fun deleteStreak(streak: streakModel)

    @Query("SELECT * FROM streaks")
    suspend fun getAllStreaks(): List<streakModel>

    @Query("SELECT * FROM streaks WHERE streakId = :id")
    suspend fun getStreakById(id: Int): streakModel?
}