package com.example.pomodo.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.data.UserRepository
import com.example.pomodo.model.StatsData
import com.example.pomodo.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val stats: StatsData = StatsData(0, 0, 0, 0),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(private val repo: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val uid = repo.getCurrentUid() ?: return@launch
            val profile = repo.getProfile(uid)
            val stats = repo.getStudyStats(uid)
            _uiState.value = ProfileUiState(profile, stats, isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
        }
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile?.copy(displayName = newName)
        )
    }

    fun onSaveName() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val uid = repo.getCurrentUid() ?: return@launch
            _uiState.value.profile?.displayName?.let {
                repo.updateProfileName(uid, it)
            }
            loadProfile()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
        }
    }

    fun onPictureSelected(uri: Uri) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val uid = repo.getCurrentUid() ?: return@launch
            val url = repo.uploadProfilePicture(uid, uri)
            repo.updateProfilePhoto(uid, url)
            loadProfile()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
        }
    }

    class Factory(private val repo: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
