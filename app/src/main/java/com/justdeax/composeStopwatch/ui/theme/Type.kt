package com.justdeax.composeStopwatch.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle( //RADIO ITEM
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    titleLarge = TextStyle( //APP NAME
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 23.sp,
    ),
    titleMedium = TextStyle( //MEDIUM SIZE TEXT IN SCREEN AWAKE DIALOG AND BOTTOM SHEET
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    )
)