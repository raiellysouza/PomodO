package com.example.pomodo.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pomodo.PomodoroViewModel
import com.example.pomodo.model.PomodoroTimer
import com.example.pomodo.ui.theme.ThemeViewModel
import com.example.pomodo.ui.timer.PomodoroTimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteTimersScreen(
    pomodoroViewModel: PomodoroViewModel,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current.applicationContext as Application
    val customTimerViewModel: PomodoroTimerViewModel = viewModel(factory = PomodoroTimerViewModel.Factory(context))

    val availableCustomTimers by customTimerViewModel.availableCustomTimers.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedTimerToEdit by remember { mutableStateOf<PomodoroTimer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meus Timers Favoritos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                selectedTimerToEdit = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Timer")
                Spacer(Modifier.width(8.dp))
                Text("Adicionar")
            }
            Button(onClick = { customTimerViewModel.refreshTimers() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Atualizar Timers")
                Spacer(Modifier.width(8.dp))
                Text("Atualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (availableCustomTimers.isEmpty()) {
            Text(
                text = "Nenhum timer personalizado salvo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(availableCustomTimers) { timer ->
                    TimerCustomizationCard(
                        timer = timer,
                        onEdit = { editedTimer ->
                            selectedTimerToEdit = editedTimer
                            showAddEditDialog = true
                        },
                        onDelete = { deletedTimer ->
                            if (deletedTimer.id != null) {
                                customTimerViewModel.deletePomodoroTimer(deletedTimer.id!!)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditTimerDialog(
            timer = selectedTimerToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { timerConfirmed ->
                if (timerConfirmed.id == null) {
                    customTimerViewModel.addPomodoroTimer(timerConfirmed)
                } else {
                    customTimerViewModel.updatePomodoroTimer(timerConfirmed)
                }
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun TimerCustomizationCard(
    timer: PomodoroTimer,
    onEdit: (PomodoroTimer) -> Unit,
    onDelete: (PomodoroTimer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Foco: ${timer.focusMinutes} min", style = MaterialTheme.typography.bodySmall)
                Text(text = "Pausa Curta: ${timer.shortBreakMinutes} min", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = { onEdit(timer) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar Timer")
                }
                IconButton(onClick = { onDelete(timer) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir Timer")
                }
            }
        }
    }
}

@Composable
fun AddEditTimerDialog(
    timer: PomodoroTimer?,
    onDismiss: () -> Unit,
    onConfirm: (PomodoroTimer) -> Unit
) {
    var timerName by remember { mutableStateOf(timer?.name ?: "") }
    var focusMinutes by remember { mutableStateOf(timer?.focusMinutes ?: 25) }
    var shortBreakMinutes by remember { mutableStateOf(timer?.shortBreakMinutes ?: 5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (timer == null) "Adicionar Novo Timer" else "Editar Timer") },
        text = {
            Column {
                OutlinedTextField(
                    value = timerName,
                    onValueChange = { timerName = it },
                    label = { Text("Nome do Timer") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Text("Foco: $focusMinutes minutos")
                Slider(
                    value = focusMinutes.toFloat(),
                    onValueChange = { focusMinutes = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 58,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Pausa Curta: $shortBreakMinutes minutos")
                Slider(
                    value = shortBreakMinutes.toFloat(),
                    onValueChange = { shortBreakMinutes = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 28,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newTimer = PomodoroTimer(
                    id = timer?.id,
                    name = timerName,
                    focusMinutes = focusMinutes,
                    shortBreakMinutes = shortBreakMinutes
                )
                onConfirm(newTimer)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
