package com.example.telechat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Blue = Color(0xFF2AABEE)

private val LightColors = lightColorScheme(
    primary = Blue, onPrimary = Color.White,
    background = Color.White, surface = Color.White,
    surfaceVariant = Color(0xFFF1F3F4),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF6AB3F3), onPrimary = Color.Black,
    background = Color(0xFF0E1621), surface = Color(0xFF17212B),
    surfaceVariant = Color(0xFF1F2C3A),
)

object Bubble {
    @Composable fun outgoing() = if (isSystemInDarkTheme()) Color(0xFF2B5278) else Color(0xFFEFFDDE)
    @Composable fun incoming() = if (isSystemInDarkTheme()) Color(0xFF182533) else Color.White
    @Composable fun wallpaper() = if (isSystemInDarkTheme()) Color(0xFF0E1621) else Color(0xFFDDE6EC)
}

@Composable
fun TeleChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
