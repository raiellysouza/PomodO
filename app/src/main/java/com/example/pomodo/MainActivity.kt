package com.example.pomodo

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pomodo.notification.PomodoroNotificationChannel
import com.example.pomodo.screens.*
import com.example.pomodo.ui.auth.AuthViewModel
import com.example.pomodo.ui.profile.ProfileViewModel
import com.example.pomodo.ui.theme.PomodoTheme
import com.example.pomodo.ui.theme.ThemeViewModel
import com.example.pomodo.PomodoroViewModel
import com.example.pomodo.data.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        PomodoroNotificationChannel.createNotificationChannel(applicationContext)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val userRepository = UserRepository(auth, firestore, storage)

        setContent {
            val context = applicationContext
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val navController = rememberNavController()

            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(auth))
            val currentUser by authViewModel.currentUser.collectAsState()
            val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(context as Application))
            val isDarkTheme by themeViewModel.isDarkMode.collectAsState()

            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(context as Application))
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(userRepository))

            PomodoTheme(darkTheme = isDarkTheme) {
                val snackbarHostState = remember { SnackbarHostState() }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text(
                                text = "Menu",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Divider()
                            NavigationDrawerItem(
                                label = { Text(Screen.Settings.label) },
                                icon = { Icon(Screen.Settings.icon, contentDescription = Screen.Settings.label) },
                                selected = currentRoute == Screen.Settings.route,
                                onClick = {
                                    navController.navigate(Screen.Settings.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text(Screen.Help.label) },
                                icon = { Icon(Screen.Help.icon, contentDescription = Screen.Help.label) },
                                selected = currentRoute == Screen.Help.route,
                                onClick = {
                                    navController.navigate(Screen.Help.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                            Divider()
                            NavigationDrawerItem(
                                label = { Text("Sair") },
                                icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Sair") },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        authViewModel.signOut()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                        drawerState.close()
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = { Text(text = "PomodO", fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "Menu Lateral")
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                val items = listOf(Screen.Home, Screen.FavoriteTimers, Screen.Profile)
                                items.forEach { screen ->
                                    val selected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (screen.route != currentRoute) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                                        label = { Text(screen.label) }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable(Screen.Login.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onLoginSuccess = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate(Screen.Register.route)
                                    }
                                )
                            }
                            composable(Screen.Register.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
                                RegisterScreen(
                                    authViewModel = authViewModel,
                                    onRegistrationSuccess = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Register.route) { inclusive = true }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate(Screen.Login.route)
                                    }
                                )
                            }
                            composable(Screen.Home.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
                                HomeScreen(pomodoroViewModel = pomodoroViewModel)
                            }
                            composable(Screen.FavoriteTimers.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
                                FavoriteTimersScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
                            }
                            composable(Screen.Profile.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
                                ProfileScreen(
                                    onBack = { navController.popBackStack() },
                                    viewModel = profileViewModel
                                )
                            }
                            composable(Screen.Settings.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
                                SettingsScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
                            }
                            composable(Screen.Help.route, enterTransition = { slideInHorizontally { it } + fadeIn() }, exitTransition = { slideOutHorizontally { -it } + fadeOut() }) {
                                HelpScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
