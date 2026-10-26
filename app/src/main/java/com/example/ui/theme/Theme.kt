package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun ApisSmartTheme(
    themeConfig: AppThemeConfig = AppThemeConfig(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeConfig.themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val palette = themeConfig.primaryPalette
    val fontColor = if (isDark) themeConfig.fontColorOption.darkColor else themeConfig.fontColorOption.lightColor
    val subtitleColor = if (isDark) themeConfig.subtitleColorOption.darkColor else themeConfig.subtitleColorOption.lightColor

    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDark) {
        darkColorScheme(
            primary = palette.primaryDark,
            onPrimary = Color(0xFF1E1B18),
            primaryContainer = palette.containerDark,
            onPrimaryContainer = palette.primaryDark,
            secondary = MeadowGreenSecondaryDark,
            onSecondary = Color(0xFF022C22),
            secondaryContainer = MeadowGreenContainerDark,
            onSecondaryContainer = OnMeadowGreenContainerDark,
            tertiary = Color(0xFFFDE047),
            onTertiary = Color(0xFF422006),
            tertiaryContainer = Color(0xFF713F12),
            onTertiaryContainer = Color(0xFFFEF08A),
            error = Color(0xFFF87171),
            errorContainer = Color(0xFF7F1D1D),
            onError = Color(0xFF450A0A),
            onErrorContainer = Color(0xFFFECACA),
            background = Color(0xFF0F172A),
            onBackground = fontColor,
            surface = SurfaceDarkTheme,
            onSurface = fontColor,
            surfaceVariant = SurfaceVariantDarkTheme,
            onSurfaceVariant = subtitleColor,
            outline = Color(0xFF475569)
        )
    } else {
        lightColorScheme(
            primary = palette.primaryLight,
            onPrimary = Color.White,
            primaryContainer = palette.containerLight,
            onPrimaryContainer = Color(0xFF451A03),
            secondary = MeadowGreenSecondary,
            onSecondary = Color.White,
            secondaryContainer = MeadowGreenContainer,
            onSecondaryContainer = OnMeadowGreenContainer,
            tertiary = WaxYellowTertiary,
            onTertiary = Color.White,
            tertiaryContainer = WaxYellowContainer,
            onTertiaryContainer = OnWaxYellowContainer,
            error = WarningAlertRed,
            errorContainer = WarningAlertRedContainer,
            onError = Color.White,
            onErrorContainer = OnWarningAlertRed,
            background = Color(0xFFFAFAF9),
            onBackground = fontColor,
            surface = Color(0xFFFFFFFF),
            onSurface = fontColor,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = subtitleColor,
            outline = Color(0xFFCBD5E1)
        )
    }

    val dynamicTypography = createScaledTypography(
        scaleFactor = themeConfig.fontSizeScale.scaleFactor,
        primaryTextColor = fontColor,
        subtitleTextColor = subtitleColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val mode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT
    ApisSmartTheme(
        themeConfig = AppThemeConfig(themeMode = mode),
        dynamicColor = dynamicColor,
        content = content
    )
}
