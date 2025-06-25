package com.example.pomodo.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodo.PomodoroViewModel
import com.example.pomodo.TimerState

@Composable
fun HomeScreen(pomodoroViewModel: PomodoroViewModel) {
    val currentTime by pomodoroViewModel.currentTime.collectAsState()
    val timerState by pomodoroViewModel.timerState.collectAsState()
    val focusTime by pomodoroViewModel.focusTime.collectAsState()
    val breakTime by pomodoroViewModel.breakTime.collectAsState()
    val currentMode by pomodoroViewModel.currentMode.collectAsState()

    var showAdjustFocusDialog by remember { mutableStateOf(false) }
    var showAdjustBreakDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (currentMode == TimerState.FOCUS) "Foco" else "Pausa",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Cronômetro central
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = when (currentMode) {
                TimerState.FOCUS -> currentTime.toFloat() / focusTime.toFloat()
                TimerState.BREAK -> currentTime.toFloat() / breakTime.toFloat()
                else -> 0f // Should not happen in a running timer
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
                fontSize = 60.sp // Tamanho maior para o tempo
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tempos ajustáveis
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
                    text = "Pausa",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${breakTime / 60} min",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAdjustBreakDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botões de controle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { pomodoroViewModel.startTimer() },
                enabled = timerState != TimerState.RUNNING,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar", color = Color.White)
            }
            Button(
                onClick = { pomodoroViewModel.pauseTimer() },
                enabled = timerState == TimerState.RUNNING,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Pausar", color = Color.White)
            }
            Button(
                onClick = { pomodoroViewModel.resetTimer() },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Resetar", color = Color.White)
            }
        }
    }

    // Diálogo para ajustar tempo de foco
    if (showAdjustFocusDialog) {
        TimeAdjustmentDialog(
            title = "Ajustar Tempo de Foco",
            initialValue = focusTime / 60,
            onDismiss = { showAdjustFocusDialog = false },
            onConfirm = { minutes ->
                pomodoroViewModel.adjustFocusTime(minutes)
                showAdjustFocusDialog = false
            }
        )
    }

    // Diálogo para ajustar tempo de pausa
    if (showAdjustBreakDialog) {
        TimeAdjustmentDialog(
            title = "Ajustar Tempo de Pausa",
            initialValue = breakTime / 60,
            onDismiss = { showAdjustBreakDialog = false },
            onConfirm = { minutes ->
                pomodoroViewModel.adjustBreakTime(minutes)
                showAdjustBreakDialog = false
            }
        )
    }
}

@Composable
fun TimeAdjustmentDialog(
    title: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = selectedMinutes.toFloat(),
                    onValueChange = { selectedMinutes = it.toInt() },
                    valueRange = 1f..60f, // Min 1 minute, Max 60 minutes
                    steps = 58 // (60 - 1) - 1 steps
                )
                Text(text = "$selectedMinutes minutos", style = MaterialTheme.typography.headlineSmall)
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