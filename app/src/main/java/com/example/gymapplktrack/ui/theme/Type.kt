package com.example.gymapplktrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val aggressiveFamily = FontFamily.SansSerif

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.2.sp,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = aggressiveFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp
    )
)
