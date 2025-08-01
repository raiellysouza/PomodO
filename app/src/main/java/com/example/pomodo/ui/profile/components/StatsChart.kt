package com.example.pomodo.ui.profile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodo.model.StatsData
import kotlin.math.max

@Composable
fun StatsChart(stats: StatsData, modifier: Modifier = Modifier) {
    val labels = listOf("Diário", "Semanal", "Mensal", "90 dias")
    val values = listOf(stats.daily, stats.weekly, stats.monthly, stats.last90Days)
    val maxValue = max(values.maxOrNull() ?: 0, 1)

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Estatísticas de Estudo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)) {

            val barWidth = size.width / (values.size * 2)
            val spacing = barWidth
            val chartHeight = size.height

            values.forEachIndexed { index, value ->
                val barHeight = (value.toFloat() / maxValue) * chartHeight
                val x = spacing + index * (barWidth + spacing)
                val y = chartHeight - barHeight

                drawRoundRect(
                    color = Color(0xFF8E24AA),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
