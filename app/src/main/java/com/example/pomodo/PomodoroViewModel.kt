package com.example.pomodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerState {
    STOPPED, RUNNING, PAUSED, FOCUS, BREAK
}

class PomodoroViewModel : ViewModel() {
    // Tempos ajustáveis
    private val _focusTime = MutableStateFlow(25 * 60) // 25 minutos em segundos
    val focusTime = _focusTime.asStateFlow()

    private val _breakTime = MutableStateFlow(5 * 60) // 5 minutos em segundos
    val breakTime = _breakTime.asStateFlow()

    // Estado do cronômetro
    private val _currentTime = MutableStateFlow(25 * 60) // Tempo inicial, igual ao foco
    val currentTime = _currentTime.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState = _timerState.asStateFlow()

    private val _currentMode = MutableStateFlow(TimerState.FOCUS) // Foco ou Pausa
    val currentMode = _currentMode.asStateFlow()

    // Configurações
    private val _isDarkTheme = MutableStateFlow(false) // Padrão: tema claro
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    // Inicializa o tempo corrente para o modo de foco
    init {
        _currentTime.value = _focusTime.value
    }

    fun startTimer() {
        if (_timerState.value == TimerState.STOPPED || _timerState.value == TimerState.PAUSED) {
            _timerState.value = TimerState.RUNNING
            timerJob?.cancel() // Cancela qualquer trabalho de timer anterior

            timerJob = viewModelScope.launch {
                while (_currentTime.value > 0 && _timerState.value == TimerState.RUNNING) {
                    delay(1000) // Espera 1 segundo
                    _currentTime.value--
                }

                if (_currentTime.value == 0) {
                    // Timer finished, switch mode
                    if (_currentMode.value == TimerState.FOCUS) {
                        _currentMode.value = TimerState.BREAK
                        _currentTime.value = _breakTime.value
                        sendNotification("Pausa iniciada!")
                    } else {
                        _currentMode.value = TimerState.FOCUS
                        _currentTime.value = _focusTime.value
                        sendNotification("Foco iniciado!")
                    }
                    _timerState.value = TimerState.STOPPED // Reseta o estado para parado após o ciclo
                    startTimer() // Inicia o próximo ciclo automaticamente
                }
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel() // Pausa o trabalho do timer
        }
    }

    fun resetTimer() {
        timerJob?.cancel() // Cancela o trabalho do timer
        _timerState.value = TimerState.STOPPED
        _currentMode.value = TimerState.FOCUS
        _currentTime.value = _focusTime.value // Volta para o tempo de foco
    }

    fun adjustFocusTime(minutes: Int) {
        _focusTime.value = minutes * 60
        if (_currentMode.value == TimerState.FOCUS && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _focusTime.value
        }
    }

    fun adjustBreakTime(minutes: Int) {
        _breakTime.value = minutes * 60
        if (_currentMode.value == TimerState.BREAK && _timerState.value == TimerState.STOPPED) {
            _currentTime.value = _breakTime.value
        }
    }

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    // Função mock para enviar notificações (em um app real, usaria o NotificationManager)
    private fun sendNotification(message: String) {
        if (_notificationsEnabled.value) {
            println("Notificação: $message") // Exemplo: log para depuração
            // Em um aplicativo real, aqui você usaria NotificationManager para exibir uma notificação.
            // Ex: context.notificationManager.notify(...)
        }
    }
}