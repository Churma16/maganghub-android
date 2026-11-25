package id.codes.maganghub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = DangerRed,
    errorContainer = DangerRedContainer,
    onError = PureWhite,
    onErrorContainer = DangerRedText
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = DangerRed,
    errorContainer = DangerRedContainer,
    onError = PureWhite,
    onErrorContainer = DangerRedText
)

@Composable
private fun ColorScheme.animateColors(durationMillis: Int = 300): ColorScheme {
    val animSpec = tween<Color>(durationMillis)
    val primary by animateColorAsState(primary, animSpec, label = "primary")
    val onPrimary by animateColorAsState(onPrimary, animSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(primaryContainer, animSpec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(onPrimaryContainer, animSpec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(secondary, animSpec, label = "secondary")
    val onSecondary by animateColorAsState(onSecondary, animSpec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(secondaryContainer, animSpec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(onSecondaryContainer, animSpec, label = "onSecondaryContainer")
    val background by animateColorAsState(background, animSpec, label = "background")
    val onBackground by animateColorAsState(onBackground, animSpec, label = "onBackground")
    val surface by animateColorAsState(surface, animSpec, label = "surface")
    val onSurface by animateColorAsState(onSurface, animSpec, label = "onSurface")
    val surfaceVariant by animateColorAsState(surfaceVariant, animSpec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(onSurfaceVariant, animSpec, label = "onSurfaceVariant")
    val outline by animateColorAsState(outline, animSpec, label = "outline")
    val error by animateColorAsState(error, animSpec, label = "error")
    val errorContainer by animateColorAsState(errorContainer, animSpec, label = "errorContainer")
    val onError by animateColorAsState(onError, animSpec, label = "onError")
    val onErrorContainer by animateColorAsState(onErrorContainer, animSpec, label = "onErrorContainer")

    return copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        error = error,
        errorContainer = errorContainer,
        onError = onError,
        onErrorContainer = onErrorContainer
    )
}

@Composable
fun MagangHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val animatedColorScheme = targetColorScheme.animateColors(durationMillis = 300)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = animatedColorScheme.background.toArgb()
            window.navigationBarColor = animatedColorScheme.surface.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
