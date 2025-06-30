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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ajuda (FAQ)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val faqItems = remember {
            listOf(
                FAQItem(
                    question = "O que é a técnica Pomodoro?",
                    answer = "A técnica Pomodoro é um método de gerenciamento de tempo que utiliza um cronômetro para dividir o trabalho em intervalos de 25 minutos, separados por breves pausas."
                ),
                FAQItem(
                    question = "Como ajustar o tempo de foco e pausa?",
                    answer = "Na tela principal, clique sobre o tempo exibido para 'Foco' ou 'Pausa'. Um diálogo aparecerá permitindo que você ajuste a duração."
                ),
                FAQItem(
                    question = "Posso usar o modo escuro?",
                    answer = "Sim! Vá para a tela de 'Configurações' e ative o 'Modo Escuro' através do switch."
                ),
                FAQItem(
                    question = "O aplicativo me notifica sobre os ciclos?",
                    answer = "Sim, se as notificações estiverem ativadas nas 'Configurações', você receberá avisos ao iniciar/encerrar pausas e ciclos Pomodoro."
                ),
                FAQItem(
                    question = "O que fazer quando o tempo de foco acaba?",
                    answer = "Quando o tempo de foco termina, o aplicativo automaticamente muda para o modo de pausa. Utilize esse tempo para relaxar e recarregar as energias."
                )
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            faqItems.forEach { item ->
                FAQCard(item = item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FAQCard(item: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

data class FAQItem(val question: String, val answer: String)
