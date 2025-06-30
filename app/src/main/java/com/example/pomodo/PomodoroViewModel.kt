package com.example.pomodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.data.PomodoroTimerRepository
import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.notification.AlarmScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class TimerState {
    STOPPED, RUNNING, PAUSED
}

enum class TimerMode {
    FOCUS, SHORT_BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)
    private val pomodoroTimerRepository: PomodoroTimerRepository

    private val _focusTime = MutableStateFlow(25 * 60)
    val focusTime: StateFlow<Int> = _focusTime.asStateFlow()

    private val _shortBreakTime = MutableStateFlow(5 * 60)
    val shortBreakTime: StateFlow<Int> = _shortBreakTime.asStateFlow()

    private val _cyclesCompleted = MutableStateFlow(0)
    val cyclesCompleted: StateFlow<Int> = _cyclesCompleted.asStateFlow()

    private val _currentTime = MutableStateFlow(25 * 60)
    val currentTime: StateFlow<Int> = _currentTime.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _currentMode = MutableStateFlow(TimerMode.FOCUS)
    val currentMode: StateFlow<TimerMode> = _currentMode.asStateFlow()

    private val _availableTimers = MutableStateFlow<List<PomodoroTimer>>(emptyList())
    val availableTimers: StateFlow<List<PomodoroTimer>> = _availableTimers.asStateFlow()

    private val _selectedPomodoroTimer = MutableStateFlow<PomodoroTimer?>(null)
    val selectedPomodoroTimer: StateFlow<PomodoroTimer?> = _selectedPomodoroTimer.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        val firestoreInstance = FirebaseFirestore.getInstance()
        val firebaseAuthInstance = FirebaseAuth.getInstance()

        pomodoroTimerRepository = PomodoroTimerRepository(firestoreInstance, firebaseAuthInstance)

        viewModelScope.launch {
            pomodoroTimerRepository.pomodoroTimers.collectLatest { timers ->
                _availableTimers.value = timers
                if (_selectedPomodoroTimer.value == null && timers.isNotEmpty()) {
                    selectPomodoroTimer(timers.first())
                } else if (timers.isEmpty()) {
                    resetToDefaultTimes()
                }
            }
        }

        viewModelScope.launch {
            pomodoroTimerRepository.refreshTimers()
        }

        resetTimer()
    }

    fun startTimer() {
        if (_timerState.value == TimerState.STOPPED || _timerState.value == TimerState.PAUSED) {
            _timerState.value = TimerState.RUNNING
            timerJob?.cancel()

            timerJob = viewModelScope.launch {
                while (_currentTime.value > 0 && _timerState.value == TimerState.RUNNING) {
                    delay(1000)
                    _currentTime.value--
                }

                if (_currentTime.value == 0) {
                    handleTimerCompletion()
                }
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
            alarmScheduler.cancelAlarm(1001)
            alarmScheduler.cancelAlarm(1002)
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.STOPPED
        _currentMode.value = TimerMode.FOCUS
        _cyclesCompleted.value = 0
        _currentTime.value = _selectedPomodoroTimer.value?.focusMinutes?.times(60) ?: _focusTime.value
        alarmScheduler.cancelAlarm(1001)
        alarmScheduler.cancelAlarm(1002)
    }

    private fun handleTimerCompletion() {
        when (_currentMode.value) {
            TimerMode.FOCUS -> {
                _currentMode.value = TimerMode.SHORT_BREAK
                _currentTime.value = _shortBreakTime.value
                sendNotification(
                    "Tempo de Foco Encerrado!",
                    "Sua sessão de foco terminou. É hora de uma pausa curta!",
                    1002
                )
            }
            TimerMode.SHORT_BREAK -> {
                _currentMode.value = TimerMode.FOCUS
                _currentTime.value = _focusTime.value
                sendNotification(
                    "Pausa Encerrada!",
                    "Sua pausa terminou. Hora de voltar ao foco!",
                    1001
                )
            }
        }
        _timerState.value = TimerState.STOPPED
        startTimer()
    }

    fun selectPomodoroTimer(timer: PomodoroTimer) {
        _selectedPomodoroTimer.value = timer
        _focusTime.value = timer.focusMinutes * 60
        _shortBreakTime.value = timer.shortBreakMinutes * 60
        if (_timerState.value == TimerState.STOPPED && _currentMode.value == TimerMode.FOCUS) {
            _currentTime.value = _focusTime.value
        }
        when (_currentMode.value) {
            TimerMode.FOCUS -> _currentTime.value = _focusTime.value
            TimerMode.SHORT_BREAK -> _currentTime.value = _shortBreakTime.value
        }
    }

    fun addPomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            pomodoroTimerRepository.addTimer(timer)
        }
    }

    fun updatePomodoroTimer(timer: PomodoroTimer) {
        viewModelScope.launch {
            pomodoroTimerRepository.updateTimer(timer)
        }
    }

    fun deletePomodoroTimer(timerId: String) {
        viewModelScope.launch {
            pomodoroTimerRepository.deleteTimer(timerId)
        }
    }

    fun refreshTimersFromBackend() {
        viewModelScope.launch {
            pomodoroTimerRepository.refreshTimers()
        }
    }

    fun adjustFocusTime(minutes: Int) {
        _focusTime.value = minutes * 60
        if (_currentMode.value == TimerMode.FOCUS && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _focusTime.value
        }
        _selectedPomodoroTimer.value = null
    }

    fun adjustShortBreakTime(minutes: Int) {
        _shortBreakTime.value = minutes * 60
        if (_currentMode.value == TimerMode.SHORT_BREAK && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _shortBreakTime.value
        }
        _selectedPomodoroTimer.value = null
    }

    private fun resetToDefaultTimes() {
        _focusTime.value = 25 * 60
        _shortBreakTime.value = 5 * 60
        if (_timerState.value == TimerState.STOPPED && _currentMode.value == TimerMode.FOCUS) {
            _currentTime.value = _focusTime.value
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        if (_notificationsEnabled.value) {
            alarmScheduler.scheduleAlarm(
                delayMillis = 0L,
                title = title,
                message = message,
                notificationId = notificationId
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PomodoroViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
