package com.example.pomodo.data

import com.example.pomodo.local.PomodoroTimerDao
import com.example.pomodo.local.PomodoroTimerEntity
import com.example.pomodo.local.toDomain
import com.example.pomodo.local.toEntity
import com.example.pomodo.model.PomodoroTimer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PomodoroTimerRepository(
    private val dao: PomodoroTimerDao,
    private val firestore: FirebaseFirestore
) {

    fun getAllTimersFlow(): Flow<List<PomodoroTimer>> {
        return dao.getAllTimers().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertTimer(timer: PomodoroTimer) {
        dao.insertTimer(timer.toEntity(isSynced = false))
        syncUnsyncedTimers()
    }

    suspend fun deleteTimer(timer: PomodoroTimer) {
        timer.id.takeIf { it.isNotEmpty() }?.let { remoteId ->
            firestore.collection("pomodoroTimers").document(remoteId).delete()
        }
        // Apaga local (pode usar remoteId ou buscar local)
        dao.deleteTimerByRemoteId(timer.id)
    }

    suspend fun syncUnsyncedTimers() {
        val unsynced = dao.getUnsyncedTimers()
        unsynced.collect { list ->
            list.forEach { entity ->
                val timer = entity.toDomain()
                val docRef = if (timer.id.isNotEmpty()) {
                    firestore.collection("pomodoroTimers").document(timer.id)
                } else {
                    firestore.collection("pomodoroTimers").document()
                }
                val id = docRef.id
                val timerWithId = timer.copy(id = id)
                docRef.set(timerWithId).addOnSuccessListener {
                    // Atualiza o local marcando como sincronizado e atualizando remoteId
                    val updatedEntity = entity.copy(id = entity.id, remoteId = id, isSynced = true)
                    // Usar coroutine para update no banco
                    // Importante: Atualize com DAO
                }
            }
        }
    }

    fun listenToRemoteChanges(onChanged: (List<PomodoroTimer>) -> Unit) {
        firestore.collection("pomodoroTimers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val timers = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PomodoroTimer::class.java)?.copy(id = doc.id)
                }
                onChanged(timers)
            }
    }
}
