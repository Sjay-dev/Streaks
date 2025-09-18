package com.example.streaks.Model

import com.example.streaks.Model.DataBase.StreakDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class StreakRepository @Inject constructor(
    private val dao: StreakDao
) {
    suspend fun insertStreak(streak: StreakModel) : Int {

     return   dao.addStreak(streak).toInt()
    }

    suspend fun updateStreak(streak: StreakModel) {
        dao.updateStreak(streak)
    }

    suspend fun deleteStreak(streak: StreakModel) {
        dao.deleteStreak(streak)
    }

     fun getAllStreaks(): Flow<List<StreakModel>> {
        return dao.getAllStreaks()
    }

    suspend fun getStreakById(id: Int): StreakModel? {
        return dao.getStreakById(id)
    }

    suspend fun endStreak(streakId: Int) {
        val streak = dao.getStreakById(streakId) ?: return
        val endedStreak = streak.copy(endDate = LocalDate.now().minusDays(1))
        dao.updateStreak(endedStreak)
    }


}



