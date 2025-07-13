package com.mai.driveapp.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mai.driveapp.Language

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E88E5),       // Blue 600
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF), // Light Blue 100
    onPrimaryContainer = Color(0xFF0D47A1), // Blue 900
    
    secondary = Color(0xFF26A69A),     // Teal 400
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB), // Teal 100
    onSecondaryContainer = Color(0xFF004D40), // Teal 900
    
    background = Color(0xFFFAFAFA),    // Grey 50
    onBackground = Color(0xFF212121),  // Grey 900
    
    surface = Color.White,
    onSurface = Color(0xFF212121),     // Grey 900
    surfaceVariant = Color(0xFFEEEEEE), // Grey 200
    onSurfaceVariant = Color(0xFF757575), // Grey 600
    
    error = Color(0xFFD32F2F),         // Red 700
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2), // Red 100
    onErrorContainer = Color(0xFFB71C1C), // Red 900
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),       // Blue 300
    onPrimary = Color(0xFF0D47A1),     // Blue 900
    primaryContainer = Color(0xFF1565C0), // Blue 800
    onPrimaryContainer = Color(0xFFE3F2FD), // Blue 50
    
    secondary = Color(0xFF4DB6AC),     // Teal 300
    onSecondary = Color(0xFF004D40),   // Teal 900
    secondaryContainer = Color(0xFF00897B), // Teal 600
    onSecondaryContainer = Color(0xFFE0F2F1), // Teal 50
    
    background = Color(0xFF121212),    // Dark background
    onBackground = Color(0xFFEEEEEE),  // Grey 200
    
    surface = Color(0xFF1E1E1E),       // Dark surface
    onSurface = Color(0xFFEEEEEE),     // Grey 200
    surfaceVariant = Color(0xFF2D2D2D), // Dark surface variant
    onSurfaceVariant = Color(0xFFBDBDBD), // Grey 400
    
    error = Color(0xFFEF5350),         // Red 400
    onError = Color(0xFF212121),       // Grey 900
    errorContainer = Color(0xFFB71C1C), // Red 900
    onErrorContainer = Color(0xFFFFEBEE), // Red 50
)

@Composable
fun DriveAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    
    // Get current language to set layout direction
    val languageManager = LocalLanguageManager.current
    val currentLanguage by languageManager.languageState
    
    // Set layout direction based on language
    val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
} 