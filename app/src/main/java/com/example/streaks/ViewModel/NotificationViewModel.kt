package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.NotificationRepository
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.NotificationModel
import com.example.streaks.Model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
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

        fun deleteNotification(notificationId: Int) {
            viewModelScope.launch {
                repository.deleteNotification(notificationId)
            }
        }






    }


//Notification Tab Feature LATER

//@Composable
//fun NotificationFab(
//    onMarkAllDone: () -> Unit,
//    onCancelAll: () -> Unit,
//) {
//    var isMarkDone by rememberSaveable { mutableStateOf(true) }
//    val haptics = LocalHapticFeedback.current
//
//    Surface(
//        modifier = Modifier
//            .padding(end = 15.dp)
//            .size(56.dp)
//            .combinedClickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,
//                onClick = {
//                    if (isMarkDone) onMarkAllDone() else onCancelAll()
//                },
//                onLongClick = {
//                    isMarkDone = !isMarkDone
//                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
//                }
//            ),
//        shape = CircleShape,
//        tonalElevation = 6.dp,
//        shadowElevation = 6.dp,
//        color = if (isMarkDone) Color(0xFF4CAF50) else Color.Red // green ↔ red
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = if (isMarkDone) Icons.Default.Check else Icons.Default.Close,
//                contentDescription = if (isMarkDone) "Mark Done" else "Cancel",
//                tint = Color.White
//            )
//        }
//    }
//}