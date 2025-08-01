package com.example.pomodo.model

data class PomodoroTimer(
    var id: String? = null,
    val name: String = "",
    val focusMinutes: Int = 0,
    val shortBreakMinutes: Int = 0,
    val longBreakMinutes: Int = 0,
    val longBreakInterval: Int = 0
)

