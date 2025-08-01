package com.example.pomodo.ui.timer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.data.PomodoroTimerRepository
import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.local.AppDatabase
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
        val pomodoroTimerDaoInstance = AppDatabase.getDatabase(application).pomodoroTimerDao()

        repository = PomodoroTimerRepository(firestoreInstance, firebaseAuthInstance, pomodoroTimerDaoInstance)

        viewModelScope.launch {
            repository.pomodoroTimers.collectLatest { timers ->
                _availableCustomTimers.value = timers
            }
        }

        viewModelScope.launch {
            repository.refreshTimersFromFirebase()
        }
    }

    fun addPomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            try {
                repository.addTimer(timer)
            } catch (e: Exception) {
                Log.e("PomodoroTimerViewModel", "Erro ao adicionar timer: ${e.message}", e)
            }
        }
    }

    fun updatePomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            try {
                repository.updateTimer(timer)
            } catch (e: Exception) {
                Log.e("PomodoroTimerViewModel", "Erro ao atualizar timer: ${e.message}", e)
            }
        }
    }

    fun deletePomodoroTimer(timerId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTimer(timerId)
            } catch (e: Exception) {
                Log.e("PomodoroTimerViewModel", "Erro ao deletar timer: ${e.message}", e)
            }
        }
    }

    fun refreshTimers() {
        viewModelScope.launch {
            try {
                repository.refreshTimersFromFirebase()
            } catch (e: Exception) {
                Log.e("PomodoroTimerViewModel", "Erro ao sincronizar timers: ${e.message}", e)
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
