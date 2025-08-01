package com.example.pomodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.model.PomodoroTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerState {
    STOPPED, RUNNING, PAUSED
}

enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    // Aqui você deve injetar seu repositório que cuida de Room + Firestore (remova UserRepository)
    private val repo = PomodoroTimerRepository(application) // ajuste conforme seu repo

    private val _timers = MutableStateFlow<List<PomodoroTimer>>(emptyList())
    val availableTimers: StateFlow<List<PomodoroTimer>> = _timers.asStateFlow()

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

    private val _selectedPomodoroTimer = MutableStateFlow<PomodoroTimer?>(null)
    val selectedPomodoroTimer: StateFlow<PomodoroTimer?> = _selectedPomodoroTimer.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    private var timerJob: Job? = null

    init {
        loadTimers()
    }

    fun loadTimers() = viewModelScope.launch {
        // Carregue timers do repositório, que busca do Room e Firestore
        val list = repo.getAllTimers()
        _timers.value = list
        if (_selectedPomodoroTimer.value == null && list.isNotEmpty()) {
            selectPomodoroTimer(list.first())
        }
    }

    fun saveTimer(timer: PomodoroTimer) = viewModelScope.launch {
        repo.saveTimer(timer)
        loadTimers()
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
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.STOPPED
        _currentMode.value = TimerMode.FOCUS
        _cyclesCompleted.value = 0
        _currentTime.value = _selectedPomodoroTimer.value?.focusMinutes?.times(60) ?: _focusTime.value
    }

    private fun handleTimerCompletion() {
        when (_currentMode.value) {
            TimerMode.FOCUS -> {
                _cyclesCompleted.value++
                if (_cyclesCompleted.value > 0 && _cyclesCompleted.value % _longBreakInterval.value == 0) {
                    _currentMode.value = TimerMode.LONG_BREAK
                    _currentTime.value = _longBreakTime.value
                } else {
                    _currentMode.value = TimerMode.SHORT_BREAK
                    _currentTime.value = _shortBreakTime.value
                }
                _timerState.value = TimerState.STOPPED
                startTimer()
            }
            TimerMode.SHORT_BREAK -> {
                _currentMode.value = TimerMode.FOCUS
                _currentTime.value = _focusTime.value
                _timerState.value = TimerState.STOPPED
                startTimer()
            }
            TimerMode.LONG_BREAK -> {
                _currentMode.value = TimerMode.FOCUS
                _currentTime.value = _focusTime.value
                _cyclesCompleted.value = 0
                _timerState.value = TimerState.STOPPED
            }
        }
    }

    fun startNextMode() {
        if (_timerState.value == TimerState.STOPPED) {
            startTimer()
        }
    }

    fun selectPomodoroTimer(timer: PomodoroTimer) {
        _selectedPomodoroTimer.value = timer
        _focusTime.value = timer.focusMinutes * 60
        _shortBreakTime.value = timer.shortBreakMinutes * 60
        _longBreakTime.value = timer.longBreakMinutes * 60
        _longBreakInterval.value = timer.longBreakInterval

        if (_timerState.value == TimerState.STOPPED) {
            when (_currentMode.value) {
                TimerMode.FOCUS -> _currentTime.value = _focusTime.value
                TimerMode.SHORT_BREAK -> _currentTime.value = _shortBreakTime.value
                TimerMode.LONG_BREAK -> _currentTime.value = _longBreakTime.value
            }
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

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
                return PomodoroViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
