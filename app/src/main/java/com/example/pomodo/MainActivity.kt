package com.example.pomodo

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pomodo.notification.PomodoroNotificationChannel
import com.example.pomodo.screens.FavoriteTimersScreen
import com.example.pomodo.screens.HelpScreen
import com.example.pomodo.screens.HomeScreen
import com.example.pomodo.screens.LoginScreen
import com.example.pomodo.screens.RegisterScreen
import com.example.pomodo.screens.SettingsScreen
import com.example.pomodo.ui.theme.PomodoTheme
import com.example.pomodo.ui.theme.ThemeViewModel
import com.example.pomodo.ui.auth.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        PomodoroNotificationChannel.createNotificationChannel(applicationContext)

        setContent {
            val context = LocalContext.current.applicationContext
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val navController = rememberNavController()

            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(FirebaseAuth.getInstance()))
            val currentUser by authViewModel.currentUser.collectAsState()

            val startDestination = remember(currentUser) {
                if (currentUser != null) Screen.Home.route else Screen.Login.route
            }

            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(context as Application))
            val isDarkTheme by themeViewModel.isDarkMode.collectAsState()

            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(context as Application))


            PomodoTheme(darkTheme = isDarkTheme) {
                val snackbarHostState = remember { SnackbarHostState() }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
                            Divider()
                            NavigationDrawerItem(
                                label = { Text(Screen.Settings.label) },
                                icon = { Icon(Screen.Settings.icon, contentDescription = Screen.Settings.label) },
                                selected = currentRoute == Screen.Settings.route,
                                onClick = {
                                    navController.navigate(Screen.Settings.route)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text(Screen.Help.label) },
                                icon = { Icon(Screen.Help.icon, contentDescription = Screen.Help.label) },
                                selected = currentRoute == Screen.Help.route,
                                onClick = {
                                    navController.navigate(Screen.Help.route)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            Divider()
                            NavigationDrawerItem(
                                label = { Text("Sair") },
                                icon = { Icon(Icons.Filled.Person, contentDescription = "Sair") },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        authViewModel.signOut()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                        drawerState.close()
                                    }
                                }
                            )
                        }
                    },
                    content = {
                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(text = "PomodO", fontWeight = FontWeight.Bold)
                                    },
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
                                                if (screen.route == currentRoute) {
                                                    return@NavigationBarItem
                                                }

                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
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
                                composable(
                                    route = Screen.Login.route,
                                    enterTransition = { fadeIn() },
                                    exitTransition = { fadeOut() }
                                ) {
                                    LoginScreen(
                                        authViewModel = authViewModel,
                                        onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                                        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                                    )
                                }
                                composable(
                                    route = Screen.Register.route,
                                    enterTransition = { fadeIn() },
                                    exitTransition = { fadeOut() }
                                ) {
                                    RegisterScreen(
                                        authViewModel = authViewModel,
                                        onRegistrationSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                                    )
                                }

                                composable(
                                    route = Screen.Home.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    HomeScreen(pomodoroViewModel = pomodoroViewModel)
                                }
                                composable(
                                    route = Screen.FavoriteTimers.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    FavoriteTimersScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
                                }
                                composable(
                                    route = Screen.Profile.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    Text("Tela de Perfil (Em desenvolvimento)")
                                }

                                composable(
                                    route = Screen.Settings.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    SettingsScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
                                }
                                composable(
                                    route = Screen.Help.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    HelpScreen()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
