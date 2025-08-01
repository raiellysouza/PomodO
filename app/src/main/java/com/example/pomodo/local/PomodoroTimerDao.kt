package com.example.pomodo.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroTimerDao {
    @Query("SELECT * FROM pomodoro_timers_cache ORDER BY name ASC")
    fun getAllTimers(): Flow<List<PomodoroTimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: PomodoroTimerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timers: List<PomodoroTimerEntity>)

    @Update
    suspend fun updateTimer(timer: PomodoroTimerEntity)

    @Query("DELETE FROM pomodoro_timers_cache WHERE remoteId = :remoteId")
    suspend fun deleteTimerByRemoteId(remoteId: String)

    @Query("DELETE FROM pomodoro_timers_cache WHERE id = :localId")
    suspend fun deleteTimerByLocalId(localId: Long)

    @Query("SELECT * FROM pomodoro_timers_cache WHERE isSynced = 0")
    fun getUnsyncedTimers(): Flow<List<PomodoroTimerEntity>>

    @Query("DELETE FROM pomodoro_timers_cache")
    suspend fun deleteAll()
}

