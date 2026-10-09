package com.subtrack.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier
) {
    val max = slices.maxOfOrNull { it.value } ?: 0.0
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "bar_animation"
    )

    if (max <= 0.0) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Нет данных для диаграммы",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        slices.forEach { slice ->
            val fraction = (slice.value / max).toFloat() * animatedProgress

            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "${slice.value.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = slice.color
                )
                Spacer(Modifier.height(4.dp))
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val barHeight = size.height * fraction
                    val cornerRadius = 12f
                    drawRoundRect(
                        color = slice.color,
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(size.width, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    slice.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
