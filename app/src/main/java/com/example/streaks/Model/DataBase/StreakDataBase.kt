package com.example.streaks.Model.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.streaks.Model.Converters
import com.example.streaks.Model.StreakModel

@Database(
    entities = [StreakModel::class],
    version = 1,
    exportSchema = false)
@TypeConverters(Converters::class)
abstract class StreakDataBase : RoomDatabase() {


    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: StreakDataBase? = null

        fun getDatabase(context: Context): StreakDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreakDataBase::class.java,
                    "streak_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}