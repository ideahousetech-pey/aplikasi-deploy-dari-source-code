package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    onPrimary = OnPrimaryContainerPurple,
    primaryContainer = PrimaryPurple,
    onPrimaryContainer = PrimaryContainerLavender,
    secondary = PurpleGrey80,
    secondaryContainer = OnSecondaryContainerDark,
    onSecondaryContainer = SecondaryContainerLight,
    background = OnBackgroundDark,
    onBackground = BackgroundLight,
    surface = OnBackgroundDark,
    onSurface = BackgroundLight,
    surfaceVariant = OnSecondaryContainerDark,
    onSurfaceVariant = OutlineColor,
    outline = OutlineColor,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = OnPrimaryPurple,
    primaryContainer = PrimaryContainerLavender,
    onPrimaryContainer = OnPrimaryContainerPurple,
    secondary = PurpleGrey40,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundLight,
    onBackground = OnBackgroundDark,
    surface = SurfaceLight,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantMuted,
    outline = OutlineColor,
    outlineVariant = SelectedPillLavender,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Use our curated bold typography palette by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
