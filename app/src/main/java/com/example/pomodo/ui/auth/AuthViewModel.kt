package com.example.pomodo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage

    fun signIn(email: String, password: String) = viewModelScope.launch {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                _currentUser.value = auth.currentUser
                _authMessage.value = task.exception?.message
            }
    }

    fun register(email: String, password: String) = viewModelScope.launch {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                _currentUser.value = auth.currentUser
                _authMessage.value = task.exception?.message
            }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _currentUser.value = auth.currentUser
                _authMessage.value = task.exception?.message
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    class Factory(val auth: FirebaseAuth) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(auth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
