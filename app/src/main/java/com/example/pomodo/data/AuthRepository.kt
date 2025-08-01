package com.example.pomodo.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    suspend fun registerWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(userCredential.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val userCredential = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(userCredential.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val userCredential = firebaseAuth.signInWithCredential(credential).await()
            Result.success(userCredential.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

