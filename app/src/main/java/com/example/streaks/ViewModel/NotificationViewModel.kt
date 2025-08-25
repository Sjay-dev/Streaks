package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationModel>> =
        repo.getAllNotifications()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addNotification(notification: NotificationModel) {
        viewModelScope.launch {
            repo.addNotification(notification)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
        }
    }

    fun deleteNotification(notification: NotificationModel) {
        viewModelScope.launch {
            repo.deleteNotification(notification)
        }
    }
}
