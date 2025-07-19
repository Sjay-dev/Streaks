package com.example.streaks.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [streakModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class streakDataBase : RoomDatabase() {

    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: streakDataBase? = null

        fun getDatabase(context: Context): streakDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    streakDataBase::class.java,
                    "streak_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}