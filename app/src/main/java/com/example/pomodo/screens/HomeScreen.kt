package com.example.pomodo.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pomodo.PomodoroViewModel
import com.example.pomodo.TimerMode
import com.example.pomodo.TimerState
import com.example.pomodo.ui.timer.PomodoroTimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(pomodoroViewModel: PomodoroViewModel) {
    val context = LocalContext.current.applicationContext as Application
    val customTimerViewModel: PomodoroTimerViewModel = viewModel(factory = PomodoroTimerViewModel.Factory(context))

    val currentTime by pomodoroViewModel.currentTime.collectAsState()
    val timerState by pomodoroViewModel.timerState.collectAsState()
    val currentMode by pomodoroViewModel.currentMode.collectAsState()
    val cyclesCompleted by pomodoroViewModel.cyclesCompleted.collectAsState()
    val selectedPomodoroTimer by pomodoroViewModel.selectedPomodoroTimer.collectAsState()

    val focusTime by pomodoroViewModel.focusTime.collectAsState()
    val shortBreakTime by pomodoroViewModel.shortBreakTime.collectAsState()
    val longBreakTime by pomodoroViewModel.longBreakTime.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (currentMode) {
                TimerMode.FOCUS -> "Foco"
                TimerMode.SHORT_BREAK -> "Pausa Curta"
                TimerMode.LONG_BREAK -> "Pausa Longa"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val progress = when (currentMode) {
            TimerMode.FOCUS -> if (focusTime > 0) currentTime.toFloat() / focusTime.toFloat() else 0f
            TimerMode.SHORT_BREAK -> if (shortBreakTime > 0) currentTime.toFloat() / shortBreakTime.toFloat() else 0f
            TimerMode.LONG_BREAK -> if (longBreakTime > 0) currentTime.toFloat() / longBreakTime.toFloat() else 0f
        }

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            val minutes = currentTime / 60
            val seconds = currentTime % 60
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium,
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Ciclos concluídos: $cyclesCompleted", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { pomodoroViewModel.startTimer() },
                enabled = timerState != TimerState.RUNNING,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar", color = Color.White)
            }

            Button(
                onClick = { pomodoroViewModel.pauseTimer() },
                enabled = timerState == TimerState.RUNNING,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Pausar", color = Color.White)
            }

            Button(
                onClick = { pomodoroViewModel.resetTimer() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Resetar", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { pomodoroViewModel.startNextMode() },
            enabled = timerState == TimerState.STOPPED,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(
                text = if (currentMode == TimerMode.LONG_BREAK) "Iniciar Novo Ciclo" else "Iniciar Próximo",
                color = Color.White
            )
        }
    }
}
