package com.example.pomodo.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodo.model.StatsData
import com.example.pomodo.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val stats: StatsData = StatsData(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(private val repo: UserRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _ui

    fun loadProfile() = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true)
        try {
            val uid = repo.getCurrentUid() ?: return@launch
            val profile = repo.getProfile(uid) ?: UserProfile()
            val stats = repo.getStudyStats(uid) ?: StatsData()
            _ui.value = ProfileUiState(profile, stats, false, null)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(isLoading = false, errorMessage = e.message)
        }
    }

    fun onNameChange(name: String) {
        _ui.value = _ui.value.copy(profile = _ui.value.profile.copy(displayName = name))
    }

    fun onSaveName() = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true)
        repo.getCurrentUid()?.let {
            repo.updateProfileName(it, _ui.value.profile.displayName)
            loadProfile()
        }
    }

    fun onPictureSelected(uri: Uri) = viewModelScope.launch {
        _ui.value = _ui.value.copy(isLoading = true)
        repo.getCurrentUid()?.let {
            val url = repo.uploadProfilePicture(it, uri)
            repo.updateProfilePhoto(it, url)
            loadProfile()
        }
    }

    class Factory(val repo: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(c: Class<T>) =
            ProfileViewModel(repo) as T
    }
}
