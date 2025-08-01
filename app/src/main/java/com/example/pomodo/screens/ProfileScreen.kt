package com.example.pomodo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pomodo.ui.profile.ProfileViewModel
import com.example.pomodo.ui.profile.components.ProfilePictureUploader
import com.example.pomodo.ui.profile.components.UserNameEditor
import com.example.pomodo.ui.profile.components.StatsChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfilePictureUploader(
                photoUrl = state.profile?.photoUrl ?: "",
                onPictureSelected = viewModel::onPictureSelected
            )
            UserNameEditor(
                name = state.profile?.displayName ?: "",
                onNameChange = viewModel::onNameChange,
                onSaveName = viewModel::onSaveName
            )
            StatsChart(stats = state.stats)
        }
    }
}
