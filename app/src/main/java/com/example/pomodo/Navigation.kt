package com.example.pomodo

import android.app.Application
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pomodo.screens.*
import com.example.pomodo.ui.auth.AuthViewModel
import com.example.pomodo.screens.ProfileScreen
import com.example.pomodo.ui.profile.ProfileViewModel
import com.example.pomodo.ui.theme.ThemeViewModel
import com.example.pomodo.PomodoroViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Filled.Person)
    object Register : Screen("register", "Registrar", Icons.Filled.Person)
    object Home : Screen("home", "Início", Icons.Filled.Home)
    object Settings : Screen("settings", "Configurações", Icons.Filled.Settings)
    object Help : Screen("help", "Ajuda", Icons.Filled.Info)
    object FavoriteTimers : Screen("favorite_timers", "Meus Timers", Icons.Filled.Star)
    object Profile : Screen("profile", "Perfil", Icons.Filled.Person)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PomodONavHost(navController: NavHostController, onBackProfile: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = Screen.Login.route
    val context = LocalContext.current.applicationContext
    val application = context as Application
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userRepository = UserRepository(auth, firestore, storage)

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(auth))
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(auth))
            RegisterScreen(
                authViewModel = authViewModel,
                onRegistrationSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Home.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(application))
            HomeScreen(pomodoroViewModel = pomodoroViewModel)
        }
        composable(Screen.Settings.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(application))
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(application))
            SettingsScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
        }
        composable(Screen.Help.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
            HelpScreen()
        }
        composable(Screen.FavoriteTimers.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(application))
            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(application))
            FavoriteTimersScreen(pomodoroViewModel, themeViewModel)
        }
        composable(Screen.Profile.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(userRepository))
            ProfileScreen(
                viewModel = profileViewModel,
                onBack = onBackProfile
            )
        }
    }
}
