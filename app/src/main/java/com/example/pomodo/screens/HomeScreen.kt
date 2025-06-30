package com.example.pomodo.screens

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pomodo.PomodoroViewModel
import com.example.pomodo.TimerMode
import com.example.pomodo.TimerState
import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.ui.timer.PomodoroTimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(pomodoroViewModel: PomodoroViewModel) {
    val context = LocalContext.current.applicationContext as Application
    val customTimerViewModel: PomodoroTimerViewModel = viewModel(factory = PomodoroTimerViewModel.Factory(context))

    val currentTime by pomodoroViewModel.currentTime.collectAsState()
    val timerState by pomodoroViewModel.timerState.collectAsState()
    val focusTime by pomodoroViewModel.focusTime.collectAsState()
    val shortBreakTime by pomodoroViewModel.shortBreakTime.collectAsState()
    val currentMode by pomodoroViewModel.currentMode.collectAsState()

    val availableCustomTimers by customTimerViewModel.availableCustomTimers.collectAsState()
    val selectedPomodoroTimer by pomodoroViewModel.selectedPomodoroTimer.collectAsState()

    var showAdjustFocusDialog by remember { mutableStateOf(false) }
    var showAdjustShortBreakDialog by remember { mutableStateOf(false) }

    var expandedCustomTimersMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (currentMode) {
                TimerMode.FOCUS -> "Foco"
                TimerMode.SHORT_BREAK -> "Pausa Curta"
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = when (currentMode) {
                TimerMode.FOCUS -> if (focusTime > 0) currentTime.toFloat() / focusTime.toFloat() else 0f
                TimerMode.SHORT_BREAK -> if (shortBreakTime > 0) currentTime.toFloat() / shortBreakTime.toFloat() else 0f
            }

            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            val minutes = currentTime / 60
            val seconds = currentTime % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Foco",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${focusTime / 60} min",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAdjustFocusDialog = true }
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pausa Curta",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${shortBreakTime / 60} min",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAdjustShortBreakDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { expandedCustomTimersMenu = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Text(selectedPomodoroTimer?.name ?: "Selecionar Timer Personalizado")
            DropdownMenu(
                expanded = expandedCustomTimersMenu,
                onDismissRequest = { expandedCustomTimersMenu = false }
            ) {
                if (availableCustomTimers.isEmpty()) {
                    DropdownMenuItem(text = { Text("Nenhum timer personalizado disponível") }, onClick = { /* No-op */ })
                } else {
                    availableCustomTimers.forEach { timer ->
                        DropdownMenuItem(
                            text = { Text(timer.name) },
                            onClick = {
                                pomodoroViewModel.selectPomodoroTimer(timer)
                                expandedCustomTimersMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { pomodoroViewModel.startTimer() },
                enabled = timerState != TimerState.RUNNING,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar", color = Color.White)
            }
            Button(
                onClick = { pomodoroViewModel.pauseTimer() },
                enabled = timerState == TimerState.RUNNING,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Pausar", color = Color.White)
            }
            Button(
                onClick = { pomodoroViewModel.resetTimer() },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Resetar", color = Color.White)
            }
        }
    }

    if (showAdjustFocusDialog) {
        TimeAdjustmentDialog(
            title = "Ajustar Tempo de Foco",
            initialValue = focusTime / 60,
            onDismiss = { showAdjustFocusDialog = false },
            onConfirm = { minutes ->
                pomodoroViewModel.adjustFocusTime(minutes)
                showAdjustFocusDialog = false
            },
            minValue = 1,
            maxValue = 60
        )
    }

    if (showAdjustShortBreakDialog) {
        TimeAdjustmentDialog(
            title = "Ajustar Tempo de Pausa Curta",
            initialValue = shortBreakTime / 60,
            onDismiss = { showAdjustShortBreakDialog = false },
            onConfirm = { minutes ->
                pomodoroViewModel.adjustShortBreakTime(minutes)
                showAdjustShortBreakDialog = false
            },
            minValue = 1,
            maxValue = 30
        )
    }
}

@Composable
fun TimeAdjustmentDialog(
    title: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    minValue: Int,
    maxValue: Int
) {
    var selectedMinutes by remember { mutableStateOf(initialValue.coerceIn(minValue, maxValue)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$selectedMinutes minutos", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = { selectedMinutes = (selectedMinutes - 1).coerceIn(minValue, maxValue) },
                        enabled = selectedMinutes > minValue
                    ) {
                        Text(text = "-", style = MaterialTheme.typography.headlineSmall)
                    }

                    Slider(
                        value = selectedMinutes.toFloat(),
                        onValueChange = { newValue -> selectedMinutes = newValue.toInt() },
                        valueRange = minValue.toFloat()..maxValue.toFloat(),
                        steps = (maxValue - minValue).coerceAtLeast(0),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { selectedMinutes = (selectedMinutes + 1).coerceIn(minValue, maxValue) },
                        enabled = selectedMinutes < maxValue
                    ) {
                        Text(text = "+", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
