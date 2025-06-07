package com.example.gymapplktrack

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeightProgressChart(records: List<ExerciseRecord>, modifier: Modifier = Modifier) {
    if (records.isEmpty()) return

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val sorted = records.sortedBy { LocalDate.parse(it.date, formatter) }
    val maxWeight = sorted.maxOf { it.weight }.coerceAtLeast(1)
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val stepX = if (sorted.size > 1) size.width / (sorted.size - 1) else size.width
            val height = size.height
            val scaleY = height / maxWeight

            val path = Path()
            sorted.forEachIndexed { index, rec ->
                val x = stepX * index
                val y = height - rec.weight * scaleY
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            sorted.forEachIndexed { index, rec ->
                val x = stepX * index
                val y = height - rec.weight * scaleY
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            sorted.forEach { rec ->
                Text(
                    text = rec.date,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

