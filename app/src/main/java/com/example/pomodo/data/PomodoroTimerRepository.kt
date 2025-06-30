package com.example.pomodo.data

import com.example.pomodo.model.PomodoroTimer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PomodoroTimerRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private var timerListener: ListenerRegistration? = null

    val pomodoroTimers: Flow<List<PomodoroTimer>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            send(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val userTimersCollection = firestore.collection("users")
            .document(userId)
            .collection("pomodoroTimers")

        timerListener = userTimersCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val timers = snapshot.documents.mapNotNull { doc ->
                        try {
                            val timer = doc.toObject(PomodoroTimer::class.java)
                            timer?.copy(id = doc.id)
                        } catch (e: Exception) {
                            println("Erro ao mapear documento Firestore para PomodoroTimer: ${e.message}")
                            null
                        }
                    }
                    trySend(timers).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }

        awaitClose {
            timerListener?.remove()
            timerListener = null
        }
    }

    suspend fun addTimer(timer: PomodoroTimer) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para adicionar timer.")
        }

        val timerData = mapOf(
            "name" to timer.name,
            "focusMinutes" to timer.focusMinutes,
            "shortBreakMinutes" to timer.shortBreakMinutes
        )

        firestore.collection("users")
            .document(userId)
            .collection("pomodoroTimers")
            .add(timerData)
            .await()
    }

    suspend fun updateTimer(timer: PomodoroTimer) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para atualizar timer.")
        }
        if (timer.id == null) {
            throw IllegalArgumentException("ID do timer é necessário para atualização.")
        }

        val timerDocRef = firestore.collection("users")
            .document(userId)
            .collection("pomodoroTimers")
            .document(timer.id!!)

        val timerMap = mapOf(
            "name" to timer.name,
            "focusMinutes" to timer.focusMinutes,
            "shortBreakMinutes" to timer.shortBreakMinutes
        )

        timerDocRef.update(timerMap).await()
    }

    suspend fun deleteTimer(timerId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            throw IllegalStateException("Usuário não autenticado para excluir timer.")
        }

        firestore.collection("users")
            .document(userId)
            .collection("pomodoroTimers")
            .document(timerId)
            .delete()
            .await()
    }

    suspend fun refreshTimers() {
        println("Firestore refreshTimers called. Listener handles real-time updates.")
    }
}
