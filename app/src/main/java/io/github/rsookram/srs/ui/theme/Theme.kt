package io.github.rsookram.srs.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun SrsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val spec = tween<Color>(durationMillis = 600)

    val primary by animateColorAsState(Color(if (darkTheme) 0xFFFFFFFF else 0xFF121212), spec)
    val primaryVariant by animateColorAsState(
        Color(if (darkTheme) 0xFFE0E0E0 else 0xFF000000),
        spec,
    )
    val secondary by animateColorAsState(Color(if (darkTheme) 0xFF757575 else 0xFFE0E0E0), spec)
    val background by animateColorAsState(Color(if (darkTheme) 0xFF121212 else 0xFFFFFFFF), spec)
    val error by animateColorAsState(Color(if (darkTheme) 0xFFCF6679 else 0xFFB00020), spec)
    val onPrimary by animateColorAsState(if (darkTheme) Color.Black else Color.White, spec)
    val onSecondary by animateColorAsState(if (darkTheme) Color.Black else Color.Black, spec)
    val onBackground by animateColorAsState(if (darkTheme) Color.White else Color.Black, spec)
    val onError by animateColorAsState(if (darkTheme) Color.Black else Color.White, spec)

    MaterialTheme(
        Colors(
            primary,
            primaryVariant,
            secondary,
            secondaryVariant = secondary,
            background,
            surface = background,
            error,
            onPrimary,
            onSecondary,
            onBackground,
            onSurface = onBackground,
            onError,
            isLight = onPrimary.luminance() > 0.5f,
        ),
        content = content,
    )
}
