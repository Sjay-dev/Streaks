package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.DataBase.NotificationRepository
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationModel>> =
        repository.getAllNotifications()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNotification(streakId: Int, streakName: String, message: String, status: Status , frequency: Frequency) {
        viewModelScope.launch {
            repository.addNotification(
                NotificationModel(
                    streakId = streakId,
                    streakName = streakName,
                    message = message,
                    status = status,
                    frequency = frequency
                )
            )
        }
    }

    fun updateStatus(notificationId: Int, status: Status) {
        viewModelScope.launch {
            repository.updateNotificationStatus(notificationId, status)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

}