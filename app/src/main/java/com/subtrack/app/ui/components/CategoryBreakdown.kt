package com.subtrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryBreakdown(slices: List<ChartSlice>) {
    val total = slices.sumOf { it.value }
    if (total <= 0.0) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slices.sortedByDescending { it.value }.forEach { slice ->
            val percent = (slice.value / total * 100).toInt()
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(slice.color, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    slice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$percent%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "${slice.value.toInt()} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
