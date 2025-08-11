package com.example.streaks.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streaks.Model.StreakModel
import com.example.streaks.Model.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class StreakViewModel @Inject constructor
    (private val repository: StreakRepository) : ViewModel() {

    private val _streaks = MutableStateFlow<List<StreakModel>>(emptyList())
    val streaks: StateFlow<List<StreakModel>> = _streaks

    init {
        loadStreaks()
    }

    private fun loadStreaks() {
        viewModelScope.launch {
            _streaks.value = repository.getAllStreaks()
        }
    }

    fun addStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.insertStreak(streak)
        }
    }

    fun updateStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.updateStreak(streak)
            loadStreaks()
        }
    }

    fun deleteStreak(streak: StreakModel) {
        viewModelScope.launch {
            repository.deleteStreak(streak)
            loadStreaks()
        }
    }

    fun getStreakById(id: Int, callback: (StreakModel?) -> Unit) {
        viewModelScope.launch {
            callback(repository.getStreakById(id))
        }
    }
}