package com.example.pomodo.model

data class PomodoroTimer(
    val id: String = "",           // id remoto (Firestore)
    val name: String = "",
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakInterval: Int = 4
)
