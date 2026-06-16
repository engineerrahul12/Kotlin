package com.rahulsah.studio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// === STUDIO COLOR PALETTE ===
// Dark cinematic theme with neon violet accent — inspired by professional editing suites
val StudioBlack       = Color(0xFF080810)
val StudioDeepNavy    = Color(0xFF0D0D1A)
val StudioSurface     = Color(0xFF12121F)
val StudioCard        = Color(0xFF1A1A2E)
val StudioElevated    = Color(0xFF1E1E35)
val StudioBorder      = Color(0xFF2A2A45)

val StudioViolet      = Color(0xFF7C3AED)   // primary accent
val StudioVioletLight = Color(0xFF9F67FF)
val StudioVioletGlow  = Color(0xFFB892FF)
val StudioCyan        = Color(0xFF00E5FF)   // secondary accent
val StudioPink        = Color(0xFFFF4F93)   // danger / delete
val StudioGreen       = Color(0xFF00E676)   // success / download complete
val StudioAmber       = Color(0xFFFFAB00)   // warning / in-progress

val StudioTextPrimary   = Color(0xFFF0F0FF)
val StudioTextSecondary = Color(0xFF9999BB)
val StudioTextHint      = Color(0xFF55556A)

private val StudioDarkColorScheme = darkColorScheme(
    primary          = StudioViolet,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF3D1A7A),
    onPrimaryContainer = StudioVioletGlow,
    secondary        = StudioCyan,
    onSecondary      = StudioBlack,
    secondaryContainer = Color(0xFF003D45),
    onSecondaryContainer = StudioCyan,
    tertiary         = StudioPink,
    onTertiary       = Color.White,
    background       = StudioBlack,
    onBackground     = StudioTextPrimary,
    surface          = StudioSurface,
    onSurface        = StudioTextPrimary,
    surfaceVariant   = StudioCard,
    onSurfaceVariant = StudioTextSecondary,
    outline          = StudioBorder,
    error            = StudioPink,
    onError          = Color.White,
)

@Composable
fun StudioTheme(
    darkTheme: Boolean = true, // Always dark
    content: @Composable () -> Unit
) {
    val colorScheme = StudioDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = StudioBlack.toArgb()
            window.navigationBarColor = StudioBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudioTypography,
        content = content
    )
}
