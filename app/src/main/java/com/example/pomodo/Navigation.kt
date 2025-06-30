package com.example.pomodo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Filled.Home)
    object Settings : Screen("settings", "Configurações", Icons.Filled.Settings)
    object Help : Screen("help", "Ajuda", Icons.Filled.Info)
    object FavoriteTimers : Screen("favorite_timers", "Meus Timers", Icons.Filled.Star)
}
    