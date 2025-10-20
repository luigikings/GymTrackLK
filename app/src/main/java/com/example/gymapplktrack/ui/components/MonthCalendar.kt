package com.example.gymapplktrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendar(
    month: YearMonth,
    highlightedDays: Set<Int>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val firstDayOfWeek = days.firstOrNull()?.dayOfWeek ?: DayOfWeek.MONDAY
    val leadingEmpty = (firstDayOfWeek.value % 7)
    Column(modifier = modifier) {
        val weekDays = listOf("L", "M", "X", "J", "V", "S", "D")
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(weekDays) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(4.dp)
                )
            }
            items(leadingEmpty) {
                Box(modifier = Modifier.padding(4.dp))
            }
            items(days) { date ->
                val isHighlighted = highlightedDays.contains(date.dayOfMonth)
                val shape = MaterialTheme.shapes.small
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(shape)
                        .border(
                            width = 1.dp,
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = shape
                        )
                        .background(
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            shape = shape
                        )
                        .clickable { onDayClick(date) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
