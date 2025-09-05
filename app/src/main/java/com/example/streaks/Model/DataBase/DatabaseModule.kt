package com.example.streaks.Model.DataBase

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StreakDataBase {
        return Room.databaseBuilder(
            context,
            StreakDataBase::class.java,
            "streaks_db"
        ).build()
    }

    @Provides
    fun provideStreakDao(database: StreakDataBase): StreakDao {
        return database.streakDao()
    }

    @Provides
    fun provideNotificationDao(database: StreakDataBase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideNotificationRepository(
        dao: NotificationDao
    ): NotificationRepository {
        return NotificationRepository(dao)
    }
}




