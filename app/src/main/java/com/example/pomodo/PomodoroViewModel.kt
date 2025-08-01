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
import com.example.pomodo.local.AppDatabase
import kotlinx.coroutines.Job
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
    FOCUS, SHORT_BREAK, LONG_BREAK
}

class PomodoroViewModel(
    application: Application,
    private val pomodoroTimerRepository: PomodoroTimerRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    private val _focusTime = MutableStateFlow(25 * 60)
    val focusTime: StateFlow<Int> = _focusTime.asStateFlow()

    private val _shortBreakTime = MutableStateFlow(5 * 60)
    val shortBreakTime: StateFlow<Int> = _shortBreakTime.asStateFlow()

    private val _longBreakTime = MutableStateFlow(15 * 60)
    val longBreakTime: StateFlow<Int> = _longBreakTime.asStateFlow()

    private val _longBreakInterval = MutableStateFlow(4)
    val longBreakInterval: StateFlow<Int> = _longBreakInterval.asStateFlow()

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

    private var timerJob: Job? = null

    init {
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
            pomodoroTimerRepository.refreshTimersFromFirebase()
        }

        resetTimer()
    }

    fun startTimer() {
        if (_timerState.value == TimerState.STOPPED || _timerState.value == TimerState.PAUSED) {
            _timerState.value = TimerState.RUNNING
            timerJob?.cancel()

            val title = when (_currentMode.value) {
                TimerMode.FOCUS -> "Foco Iniciado!"
                TimerMode.SHORT_BREAK -> "Pausa Curta Iniciada!"
                TimerMode.LONG_BREAK -> "Pausa Longa Iniciada!"
            }
            val message = when (_currentMode.value) {
                TimerMode.FOCUS -> "Sua sessão de foco começou. Mantenha o ritmo!"
                TimerMode.SHORT_BREAK -> "Sua pausa curta começou. Relaxe um pouco!"
                TimerMode.LONG_BREAK -> "Sua pausa longa começou. Descanse bem!"
            }
            sendNotification(title, message, 1000)

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
            cancelAllAlarms()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.STOPPED
        _currentMode.value = TimerMode.FOCUS
        _cyclesCompleted.value = 0
        _currentTime.value = _selectedPomodoroTimer.value?.focusMinutes?.times(60) ?: _focusTime.value
        cancelAllAlarms()
    }

    private fun handleTimerCompletion() {
        _timerState.value = TimerState.STOPPED
        _currentTime.value = 0

        when (_currentMode.value) {
            TimerMode.FOCUS -> {
                _cyclesCompleted.value++
                sendNotification("Tempo de Foco Encerrado!", "Sua sessão de foco terminou. É hora de uma pausa!", 1001)
                if (_cyclesCompleted.value > 0 && _cyclesCompleted.value % _longBreakInterval.value == 0) {
                    _currentMode.value = TimerMode.LONG_BREAK
                    _currentTime.value = _longBreakTime.value
                    sendNotification("Parabéns! Ciclo Concluído!", "Que tal recarregar as energias e partir para o próximo desafio?", 1004)
                } else {
                    _currentMode.value = TimerMode.SHORT_BREAK
                    _currentTime.value = _shortBreakTime.value
                }
                startTimer()
            }
            TimerMode.SHORT_BREAK -> {
                sendNotification("Pausa Curta Encerrada!", "Hora de voltar ao foco!", 1002)
                _currentMode.value = TimerMode.FOCUS
                _currentTime.value = _focusTime.value
                startTimer()
            }
            TimerMode.LONG_BREAK -> {
                sendNotification("Pausa Longa Encerrada!", "Hora de iniciar um novo desafio!", 1003)
                _currentMode.value = TimerMode.FOCUS
                _currentTime.value = _focusTime.value
                _cyclesCompleted.value = 0
            }
        }
    }

    fun startNextMode() {
        if (_timerState.value == TimerState.STOPPED && _currentMode.value == TimerMode.FOCUS && _cyclesCompleted.value == 0) {
            startTimer()
        }
    }

    fun selectPomodoroTimer(timer: PomodoroTimer) {
        _selectedPomodoroTimer.value = timer
        _focusTime.value = timer.focusMinutes * 60
        _shortBreakTime.value = timer.shortBreakMinutes * 60
        _longBreakTime.value = timer.longBreakMinutes * 60
        _longBreakInterval.value = timer.longBreakInterval

        if (_timerState.value == TimerState.STOPPED || _currentMode.value == TimerMode.FOCUS) {
            _currentTime.value = _focusTime.value
            _currentMode.value = TimerMode.FOCUS
            _cyclesCompleted.value = 0
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
            pomodoroTimerRepository.refreshTimersFromFirebase()
        }
    }

    fun adjustFocusTime(minutes: Int) {
        _focusTime.value = minutes * 60
        if (_currentMode.value == TimerMode.FOCUS && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _focusTime.value
        }
        _selectedPomodoroTimer.value = null
        _cyclesCompleted.value = 0
    }

    fun adjustShortBreakTime(minutes: Int) {
        _shortBreakTime.value = minutes * 60
        if (_currentMode.value == TimerMode.SHORT_BREAK && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _shortBreakTime.value
        }
        _selectedPomodoroTimer.value = null
        _cyclesCompleted.value = 0
    }

    fun adjustLongBreakTime(minutes: Int) {
        _longBreakTime.value = minutes * 60
        if (_currentMode.value == TimerMode.LONG_BREAK && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _longBreakTime.value
        }
        _selectedPomodoroTimer.value = null
        _cyclesCompleted.value = 0
    }

    fun adjustLongBreakInterval(interval: Int) {
        _longBreakInterval.value = interval
        _selectedPomodoroTimer.value = null
        _cyclesCompleted.value = 0
    }

    private fun resetToDefaultTimes() {
        _focusTime.value = 25 * 60
        _shortBreakTime.value = 5 * 60
        _longBreakTime.value = 15 * 60
        _longBreakInterval.value = 4
        _cyclesCompleted.value = 0
        _currentMode.value = TimerMode.FOCUS
        if (_timerState.value == TimerState.STOPPED) {
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

    private fun cancelAllAlarms() {
        (1000..1004).forEach { alarmScheduler.cancelAlarm(it) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val pomodoroTimerDao = AppDatabase.getDatabase(application).pomodoroTimerDao()
                val pomodoroTimerRepository = PomodoroTimerRepository(firestore, auth, pomodoroTimerDao)
                return PomodoroViewModel(application, pomodoroTimerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
