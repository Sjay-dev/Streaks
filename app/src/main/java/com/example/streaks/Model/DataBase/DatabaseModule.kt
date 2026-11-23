package com.example.streaks.Model.DataBase

import android.content.Context
import androidx.room.Room
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.View.SettingsScreens.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StreakDataBase =
        Room.databaseBuilder(context, StreakDataBase::class.java, "streaks_db")
            .addMigrations(StreakDataBase.MIGRATION_1_2, StreakDataBase.MIGRATION_2_3, StreakDataBase.MIGRATION_3_4)
            .build()

    @Provides fun provideStreakDao(database: StreakDataBase): StreakDao = database.streakDao()
    @Provides fun provideNotificationDao(database: StreakDataBase): NotificationDao = database.notificationDao()
    @Provides fun provideNotificationRepository(dao: NotificationDao) = NotificationRepository(dao)
    @Provides @Singleton fun provideSettingsDataStore(@ApplicationContext context: Context) = SettingsDataStore(context)
}
