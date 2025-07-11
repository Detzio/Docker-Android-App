package com.example.dockerapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = DockerBlue,
    onPrimary = LightOnPrimary,
    secondary = DockerDarkBlue,
    onSecondary = LightOnSecondary,
    tertiary = DockerOrange,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = ErrorColor,
    onError = LightOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = DockerBlue,
    onPrimary = DarkOnPrimary,
    secondary = DockerDarkBlue,
    onSecondary = DarkOnSecondary,
    tertiary = DockerOrange,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = ErrorColor,
    onError = DarkOnError
)

@Composable
fun DockerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    // Utilisation de SystemUiController pour gérer la couleur de la barre d'état
    val systemUiController = rememberSystemUiController()
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Configurer les couleurs de la barre d'état avec SystemUiController
            systemUiController.setStatusBarColor(
                color = colorScheme.primary,
                darkIcons = !darkTheme
            )
            
            // Configurer la décoration de la fenêtre
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}