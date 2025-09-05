package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.DataBase.NotificationRepository
import com.example.streaks.Model.NotificationModel
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

    val notificationHistory: StateFlow<List<NotificationModel>> =
        repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(notification: NotificationModel) {
        viewModelScope.launch {
            repository.insert(notification)
        }
    }

    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
        }
    }

  fun delete(notification: NotificationModel) {
        viewModelScope.launch {
            repository.delete(notification)
        }
    }

    fun deleteById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
