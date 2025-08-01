package com.example.pomodo.model

data class PomodoroTimer(
    var id: String? = null,
    val name: String = "",
    val focusMinutes: Int = 0,
    val shortBreakMinutes: Int = 0,
    val longBreakMinutes: Int = 0,
    val longBreakInterval: Int = 0
)

data class UserProfile(
    val uid: String,
    val displayName: String,
    val photoUrl: String
)

data class StatsData(
    val daily: Long,
    val weekly: Long,
    val monthly: Long,
    val last90Days: Long
)

data class FocusStats(
    val totalPomodoros: Int = 0,
    val totalFocusMinutes: Int = 0,
    val dailyAverageMinutes: Int = 0
)


