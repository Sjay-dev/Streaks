package com.example.streaks.Model.DataBase

data class NotificationModel(
    val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
