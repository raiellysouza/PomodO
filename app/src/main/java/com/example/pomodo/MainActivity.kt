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
import com.example.pomodo.screens.SettingsScreen
import com.example.pomodo.ui.theme.PomodoTheme
import com.example.pomodo.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.example.pomodo.Screen

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

            LaunchedEffect(Unit) {
                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            println("Autenticação anônima bem-sucedida!")
                        } else {
                            println("Falha na autenticação anônima: ${task.exception}")
                        }
                    }
            }

            val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory(context as Application))
            val isDarkTheme by themeViewModel.isDarkMode.collectAsState()

            val pomodoroViewModel: PomodoroViewModel = viewModel(factory = PomodoroViewModel.Factory(context as Application))


            PomodoTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
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
                                label = { Text("Configurações") },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                                selected = currentRoute == Screen.Settings.route,
                                onClick = {
                                    navController.navigate(Screen.Settings.route)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Ajuda") },
                                icon = { Icon(Icons.Filled.Info, contentDescription = "Ajuda") },
                                selected = currentRoute == Screen.Help.route,
                                onClick = {
                                    navController.navigate(Screen.Help.route)
                                    scope.launch { drawerState.close() }
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
                                    val items = listOf(Screen.Home, Screen.FavoriteTimers)
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
                                                        inclusive = (screen == Screen.Home)
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
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(paddingValues)
                            ) {
                                composable(
                                    route = Screen.Home.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    HomeScreen(pomodoroViewModel = pomodoroViewModel)
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
                                composable(
                                    route = Screen.FavoriteTimers.route,
                                    enterTransition = { slideInHorizontally { it } + fadeIn() },
                                    exitTransition = { slideOutHorizontally { -it } + fadeOut() }
                                ) {
                                    FavoriteTimersScreen(pomodoroViewModel = pomodoroViewModel, themeViewModel = themeViewModel)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
