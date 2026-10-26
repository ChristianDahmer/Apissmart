package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createScaledTypography(
    scaleFactor: Float = 1.0f,
    primaryTextColor: Color = Color.Unspecified,
    subtitleTextColor: Color = Color.Unspecified
): Typography {
    val s = scaleFactor.coerceIn(0.8f, 1.6f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (57 * s).sp,
            lineHeight = (64 * s).sp,
            letterSpacing = (-0.25).sp,
            color = primaryTextColor
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (45 * s).sp,
            lineHeight = (52 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * s).sp,
            lineHeight = (44 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (32 * s).sp,
            lineHeight = (40 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * s).sp,
            lineHeight = (36 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * s).sp,
            lineHeight = (32 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * s).sp,
            lineHeight = (28 * s).sp,
            letterSpacing = 0.sp,
            color = primaryTextColor
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = 0.15.sp,
            color = primaryTextColor
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp,
            color = primaryTextColor
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = 0.5.sp,
            color = primaryTextColor
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.25.sp,
            color = primaryTextColor
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.4.sp,
            color = subtitleTextColor
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp,
            color = primaryTextColor
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.5.sp,
            color = subtitleTextColor
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.5.sp,
            color = subtitleTextColor
        )
    )
}

val Typography = createScaledTypography()
