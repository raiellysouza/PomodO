package com.example.pomodo.data

import android.net.Uri
import com.example.pomodo.model.StatsData
import com.example.pomodo.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun getCurrentUid(): String? = auth.currentUser?.uid

    suspend fun getProfile(uid: String): UserProfile {
        val doc = firestore.collection("users").document(uid).get().await()
        return UserProfile(
            uid = uid,
            displayName = doc.getString("displayName") ?: "",
            photoUrl = doc.getString("photoUrl") ?: ""
        )
    }

    suspend fun updateProfileName(uid: String, name: String) {
        firestore.collection("users").document(uid).update("displayName", name).await()
    }

    suspend fun updateProfilePhoto(uid: String, url: String) {
        firestore.collection("users").document(uid).update("photoUrl", url).await()
    }

    suspend fun uploadProfilePicture(uid: String, fileUri: Uri): String {
        val ref = storage.reference.child("profilePhotos/$uid.jpg")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun getStudyStats(uid: String): StatsData {
        val snap = firestore.collection("users").document(uid).collection("stats").document("summary").get().await()
        return StatsData(
            daily = snap.getLong("daily") ?: 0,
            weekly = snap.getLong("weekly") ?: 0,
            monthly = snap.getLong("monthly") ?: 0,
            last90Days = snap.getLong("last90Days") ?: 0
        )
    }

    suspend fun updateStudyStats(uid: String, stats: StatsData) {
        val data = mapOf(
            "daily" to stats.daily,
            "weekly" to stats.weekly,
            "monthly" to stats.monthly,
            "last90Days" to stats.last90Days
        )
        firestore.collection("users").document(uid).collection("stats").document("summary").set(data).await()
    }

    suspend fun savePomodoroTimer(uid: String, timerId: String, timerData: Map<String, Any>) {
        firestore.collection("users").document(uid).collection("pomodoroTimers").document(timerId).set(timerData).await()
    }

    suspend fun getPomodoroTimers(uid: String): List<Map<String, Any>> {
        val snapshot = firestore.collection("users").document(uid).collection("pomodoroTimers").get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    suspend fun deletePomodoroTimer(uid: String, timerId: String) {
        firestore.collection("users").document(uid).collection("pomodoroTimers").document(timerId).delete().await()
    }
}
