package com.example.streaks.Model.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.streaks.Model.StreakModel

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addStreak(streak: StreakModel)

    @Update
    suspend fun updateStreak(streak: StreakModel)

    @Delete
    suspend fun deleteStreak(streak: StreakModel)

    @Query("SELECT * FROM streaks")
    suspend fun getAllStreaks(): List<StreakModel>



    @Query("SELECT * FROM streaks WHERE streakId = :id")
    suspend fun getStreakById(id: Int): StreakModel?
}


