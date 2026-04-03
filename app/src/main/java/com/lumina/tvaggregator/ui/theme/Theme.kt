package com.lumina.tvaggregator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// TV-optimized dark color scheme (primary for TV usage)
private val DarkColorScheme = darkColorScheme(
    primary = TVPrimary,
    secondary = TVSecondary,
    tertiary = TVAccent,
    background = TVBackground,
    surface = TVSurface,
    onPrimary = TVOnPrimary,
    onSecondary = TVOnSecondary,
    onBackground = TVOnBackground,
    onSurface = TVOnSurface
)

// Light color scheme for non-TV devices
private val LightColorScheme = lightColorScheme(
    primary = TVPrimary,
    secondary = TVSecondary,
    tertiary = TVAccent,
    background = TVBackgroundLight,
    surface = TVSurfaceLight,
    onPrimary = TVOnPrimary,
    onSecondary = TVOnSecondary,
    onBackground = TVOnBackgroundLight,
    onSurface = TVOnSurfaceLight
)

@Composable
fun TVAggregatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for TV to ensure consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // TV apps should generally use dark theme for better viewing experience
        darkTheme || isRunningOnTV() -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TVTypography,
        content = content
    )
}

@Composable
private fun isRunningOnTV(): Boolean {
    val context = LocalContext.current
    return context.packageManager.hasSystemFeature("android.software.leanback")
}