package com.example.pomodo.data

import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.local.PomodoroTimerDao
import com.example.pomodo.local.toDomain
import com.example.pomodo.local.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class PomodoroTimerRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val pomodoroTimerDao: PomodoroTimerDao
) {
    private val _pomodoroTimers = MutableStateFlow<List<PomodoroTimer>>(emptyList())
    val pomodoroTimers: StateFlow<List<PomodoroTimer>> = _pomodoroTimers

    suspend fun refreshTimersFromFirebase() {
        val uid = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("pomodoroTimers")
            .get()
            .await()

        val timers = snapshot.documents.mapNotNull { doc ->
            doc.toObject(PomodoroTimer::class.java)?.copy(id = doc.id)
        }

        pomodoroTimerDao.deleteAll() // limpa cache local

        // Insere convertendo para entidade
        pomodoroTimerDao.insertAll(timers.map { it.toEntity(isSynced = true) })

        _pomodoroTimers.value = timers
    }

    suspend fun addTimer(timer: PomodoroTimer) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("users")
            .document(uid)
            .collection("pomodoroTimers")
            .document()

        val timerWithId = timer.copy(id = docRef.id)
        docRef.set(timerWithId).await()

        pomodoroTimerDao.insertTimer(timerWithId.toEntity(isSynced = true))

        _pomodoroTimers.value = _pomodoroTimers.value + timerWithId
    }

    suspend fun updateTimer(timer: PomodoroTimer) {
        val uid = auth.currentUser?.uid ?: return
        val remoteId = timer.id ?: return

        firestore.collection("users")
            .document(uid)
            .collection("pomodoroTimers")
            .document(remoteId)
            .set(timer)
            .await()

        pomodoroTimerDao.updateTimer(timer.toEntity(isSynced = true))

        _pomodoroTimers.value = _pomodoroTimers.value.map {
            if (it.id == remoteId) timer else it
        }
    }

    suspend fun deleteTimer(remoteId: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("pomodoroTimers")
            .document(remoteId)
            .delete()
            .await()

        pomodoroTimerDao.deleteTimerByRemoteId(remoteId)

        _pomodoroTimers.value = _pomodoroTimers.value.filter { it.id != remoteId }
    }
}
