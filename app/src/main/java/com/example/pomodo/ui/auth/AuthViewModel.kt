package com.example.pomodo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authMessage.value = null
            val result = authRepository.registerWithEmailAndPassword(email, password)
            result.onSuccess {
                _authMessage.value = "Registro bem-sucedido!"
            }.onFailure { e ->
                _authMessage.value = "Erro no registro: ${e.message}"
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authMessage.value = null
            val result = authRepository.signInWithEmailAndPassword(email, password)
            result.onSuccess {
                _authMessage.value = "Login bem-sucedido!"
            }.onFailure { e ->
                _authMessage.value = "Erro no login: ${e.message}"
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authMessage.value = null
            val result = authRepository.signInWithGoogleCredential(idToken)
            result.onSuccess {
                _authMessage.value = "Login com Google bem-sucedido!"
            }.onFailure { e ->
                _authMessage.value = "Erro no login com Google: ${e.message}"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    class Factory(private val firebaseAuth: FirebaseAuth) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(AuthRepository(firebaseAuth)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}