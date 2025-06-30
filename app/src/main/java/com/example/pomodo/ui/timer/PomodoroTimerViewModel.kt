package com.example.pomodo.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.data.PomodoroTimerRepository
import com.example.pomodo.model.PomodoroTimer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PomodoroTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PomodoroTimerRepository

    private val _availableCustomTimers = MutableStateFlow<List<PomodoroTimer>>(emptyList())
    val availableCustomTimers: StateFlow<List<PomodoroTimer>> = _availableCustomTimers.asStateFlow()

    init {
        val firestoreInstance = FirebaseFirestore.getInstance()
        val firebaseAuthInstance = FirebaseAuth.getInstance()

        repository = PomodoroTimerRepository(firestoreInstance, firebaseAuthInstance)

        viewModelScope.launch {
            repository.pomodoroTimers.collectLatest { timers ->
                _availableCustomTimers.value = timers
            }
        }

        viewModelScope.launch {
            repository.refreshTimers()
        }
    }

    fun addPomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            try {
                repository.addTimer(timer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            try {
                repository.updateTimer(timer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePomodoroTimer(timerId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTimer(timerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshTimers() {
        viewModelScope.launch {
            try {
                repository.refreshTimers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroTimerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PomodoroTimerViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
