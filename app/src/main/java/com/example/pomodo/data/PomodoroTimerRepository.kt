package com.example.pomodo.data

import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.local.PomodoroTimerDao
import com.example.pomodo.local.PomodoroTimerEntity
import com.example.pomodo.local.toDomain
import com.example.pomodo.local.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import android.util.Log

class PomodoroTimerRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val pomodoroTimerDao: PomodoroTimerDao
) {
    val pomodoroTimers: Flow<List<PomodoroTimer>> = pomodoroTimerDao.getAllTimers().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun refreshTimersFromFirebase() {
        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("pomodoroTimers")
                    .get()
                    .await()
                val remoteTimers = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(PomodoroTimer::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("PomodoroTimerRepository", "Erro ao mapear documento Firestore para PomodoroTimer: ${e.message}", e)
                        null
                    }
                }
                val remoteEntities = remoteTimers.map { it.toEntity(isSynced = true) }
                pomodoroTimerDao.deleteAll()
                pomodoroTimerDao.insertAll(remoteEntities)
                Log.d("PomodoroTimerRepository", "Timers sincronizados do Firebase para o Room.")
            } else {
                Log.d("PomodoroTimerRepository", "Usuário não autenticado, não foi possível sincronizar timers do Firebase.")
            }
        } catch (e: Exception) {
            Log.e("PomodoroTimerRepository", "Erro ao sincronizar timers do Firebase: ${e.message}", e)
        }
    }

    suspend fun addTimer(timer: PomodoroTimer) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para adicionar timer.")
        }

        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("pomodoroTimers")
                .add(timer)
                .await()

            pomodoroTimerDao.insertTimer(timer.copy(id = docRef.id).toEntity(isSynced = true))
            Log.d("PomodoroTimerRepository", "Timer '${timer.name}' adicionado ao Firestore e Room com ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("PomodoroTimerRepository", "Erro ao adicionar timer: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTimer(timer: PomodoroTimer) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para atualizar timer.")
        }
        timer.id?.let { timerId ->
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("pomodoroTimers")
                    .document(timerId)
                    .set(timer)
                    .await()

                pomodoroTimerDao.updateTimer(timer.toEntity(isSynced = true))
                Log.d("PomodoroTimerRepository", "Timer '${timer.name}' (ID: ${timerId}) atualizado no Firestore e Room.")
            } catch (e: Exception) {
                Log.e("PomodoroTimerRepository", "Erro ao atualizar timer: ${e.message}", e)
                throw e
            }
        } ?: throw IllegalArgumentException("ID do timer é necessário para atualização.")
    }

    suspend fun deleteTimer(timerId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para excluir timer.")
        }

        try {
            firestore.collection("users")
                .document(userId)
                .collection("pomodoroTimers")
                .document(timerId)
                .delete()
                .await()

            pomodoroTimerDao.deleteTimerByRemoteId(timerId)
            Log.d("PomodoroTimerRepository", "Timer (ID: $timerId) excluído do Firestore e Room.")
        } catch (e: Exception) {
            Log.e("PomodoroTimerRepository", "Erro ao excluir timer: ${e.message}", e)
            throw e
        }
    }
}
