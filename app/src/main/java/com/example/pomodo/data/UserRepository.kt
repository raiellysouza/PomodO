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
}
