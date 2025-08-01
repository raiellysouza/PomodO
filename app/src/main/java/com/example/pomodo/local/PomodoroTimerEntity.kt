package com.example.pomodo.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pomodo.model.PomodoroTimer

@Entity(tableName = "pomodoro_timers_cache")
data class PomodoroTimerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,             // ID local Room
    val remoteId: String? = null, // ID Firestore
    val name: String,
    val focusMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val longBreakInterval: Int,
    val isSynced: Boolean = false
)

fun PomodoroTimerEntity.toDomain(): PomodoroTimer {
    return PomodoroTimer(
        id = this.remoteId ?: "",
        name = this.name,
        focusMinutes = this.focusMinutes,
        shortBreakMinutes = this.shortBreakMinutes,
        longBreakMinutes = this.longBreakMinutes,
        longBreakInterval = this.longBreakInterval
    )
}

fun PomodoroTimer.toEntity(localId: Long = 0, isSynced: Boolean = false): PomodoroTimerEntity {
    return PomodoroTimerEntity(
        id = localId,
        remoteId = this.id,
        name = this.name,
        focusMinutes = this.focusMinutes,
        shortBreakMinutes = this.shortBreakMinutes,
        longBreakMinutes = this.longBreakMinutes,
        longBreakInterval = this.longBreakInterval,
        isSynced = isSynced
    )
}
