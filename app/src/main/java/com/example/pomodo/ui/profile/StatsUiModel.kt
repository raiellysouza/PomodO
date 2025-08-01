package com.example.pomodo.ui.profile

import com.example.pomodo.model.FocusStats

data class StatsUiModel(
    val totalPomodoros: Int,
    val totalFocusHours: Double,
    val dailyAverageMinutes: Int
)

fun FocusStats.toUiModel(): StatsUiModel {
    return StatsUiModel(
        totalPomodoros = totalPomodoros,
        totalFocusHours = totalFocusMinutes / 60.0,
        dailyAverageMinutes = dailyAverageMinutes
    )
}