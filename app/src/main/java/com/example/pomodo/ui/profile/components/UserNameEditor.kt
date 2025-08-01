package com.example.pomodo.ui.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserNameEditor(name: String, onNameChange: (String) -> Unit, onSaveName: () -> Unit) {
    var text by remember { mutableStateOf(name) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; onNameChange(it) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onSaveName, modifier = Modifier.align(Alignment.End)) {
            Text("Salvar")
        }
    }
}
