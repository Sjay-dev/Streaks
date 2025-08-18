package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import com.example.streaks.Model.DataBase.NotificationModel
import com.example.streaks.Model.DataBase.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    fun addNotification(title: String, message: String) {
        val notification = NotificationModel(
            id = _notifications.value.size + 1,
            title = title,
            message = message
        )
        repository.addNotification(notification)
        _notifications.value = repository.getAllNotifications()
    }
}